# NEXA — V1 (Android Mobile + Android TV)

Dois apps Android (Kotlin + Jetpack Compose) que trabalham juntos na rede Wi-Fi local:

- **NEXA Mobile** (`:mobile`, `com.nexa.mobile`): lista os apps instaláveis em "Meus Jogos", pareia com a TV (QR ou código), abre o jogo no celular, transmite tela + áudio para a TV e mostra **apenas** a tela preta de controles durante a sessão.
- **NEXA TV** (`:tv`, `com.nexa.tv`): mostra QR Code + código temporário, tela de espera/conexão e o vídeo do celular em tela cheia; navegação 100% pelo controle remoto (D-Pad).

Sem login, sem contas, sem Supabase, sem serviços pagos. Configurações e layouts de controle ficam salvos localmente (SharedPreferences).

## Estrutura

| Módulo | Conteúdo |
|---|---|
| `protocol/` | Kotlin puro (sem Android). Protocolo TCP com mensagens `[tipo][tamanho][payload]`, token/código de pareamento temporário, formato do QR. Com testes unitários. |
| `ui/` | Design system do Gemini em Compose: paleta, `NexaLogo`, cards, pills. Usado pelos dois apps. |
| `mobile/` | App do celular. |
| `tv/` | App da TV. |
| `docs/gemini-ui/NexaPrototype.jsx` | Protótipo original do Gemini (referência visual). |

## Como compilar os dois APKs

Requisitos: Android Studio (Ladybug ou mais novo) **ou** JDK 17+ com Android SDK 35 (`ANDROID_HOME` ou `local.properties` com `sdk.dir=...`).

```bash
./gradlew :mobile:assembleDebug :tv:assembleDebug
./gradlew :protocol:test        # testes do protocolo/pareamento
```

APKs gerados:

- `mobile/build/outputs/apk/debug/mobile-debug.apk`
- `tv/build/outputs/apk/debug/tv-debug.apk`

Instalação:

```bash
adb install mobile/build/outputs/apk/debug/mobile-debug.apk          # celular via USB
adb connect <IP-da-TV>:5555 && adb install tv/build/outputs/apk/debug/tv-debug.apk
```

Na TCL: Configurações → Sobre → toque 7× em "Compilação" para liberar as opções de desenvolvedor, depois ative "Depuração USB/ADB pela rede". O app aparece no launcher como **NEXA TV** (banner próprio).

Versões mínimas: Mobile Android 10 (API 29, exigido pela captura de áudio); TV Android 8 (API 26).

## Fluxo implementado

1. **TV** abre o servidor TCP (porta 47800, ou outra livre), anuncia `_nexa._tcp` na rede (DNS-SD) e mostra QR + código `NX-XXXX` com contagem de expiração.
2. **Celular** pareia:
   - **QR**: leitor do Google Play services (o NEXA não pede permissão de câmera). O QR contém `nexa://pair?h=IP&p=PORTA&t=TOKEN&n=NOME`.
   - **Código manual**: o celular procura TVs NEXA via DNS-SD e envia o código. O código **não** é anunciado na rede.
3. **Meus Jogos**: apps com ícone no launcher, com "Jogos" (categoria declarada pelo desenvolvedor) e "Todos os apps". Todos aparecem como **"Compatibilidade não verificada"**.
4. **Jogar na TV** → modal de autorização (Gemini) → permissão "Sobrepor a outros apps" → áudio/notificações (opcionais) → diálogo de captura do próprio Android.
5. O serviço em primeiro plano transmite: **vídeo H.264** (MediaCodec por hardware, 720p ou 1080p, configurável) e **áudio PCM 48 kHz estéreo** (AudioPlaybackCapture). O jogo é aberto e coberto pela tela preta de controles.
6. **TV** decodifica (MediaCodec + SurfaceView, modo baixa latência quando o decoder suporta) e toca o áudio (AudioTrack). Se atrasar, descarta quadros e pede um novo key frame.
7. **Encerrar**: pelo celular ("Encerrar"), pela notificação ou pela TV (Voltar → "Encerrar Transmissão"). A TV volta para "Dispositivo Conectado". "Desconectar" na TV desfaz o pareamento e gera um novo código.

### Segurança do pareamento

- Token aleatório de 128 bits + código curto (32⁴ combinações, sem caracteres ambíguos), **temporários**: expiram em 10 min, valem para **um** pareamento e são regenerados ao conectar, ao desconectar e após 5 tentativas erradas.
- Comparação em tempo constante. Nenhuma senha permanente é armazenada ou transmitida.
- Tráfego somente na rede local e **sem criptografia** na V1 (TCP puro).

### Package visibility

O app usa `<queries>` com o intent `MAIN/LAUNCHER`, e não `QUERY_ALL_PACKAGES`. Assim ele só vê apps com ícone no launcher, que é exatamente o que "Meus Jogos" precisa listar. Isso é aceito pela política da Play Store sem justificativa especial.

## Limitações (o que ainda precisa ser testado em aparelhos reais)

Nada desta lista foi testado em dispositivo. Este ambiente de desenvolvimento não tinha acesso ao Android SDK; veja "Verificação feita" abaixo.

1. **Controles não chegam ao jogo (não implementado de propósito).** O Android não permite que um app comum injete toques ou gamepad em outro app. A via permitida candidata é um *AccessibilityService* com `dispatchGesture`, que o usuário ativa explicitamente. Porém o gesto injetado atingiria a própria janela preta de controles, que fica por cima do jogo. Isso precisa ser validado antes de implementar. Por isso a tela de controles mostra *"Entrada em validação: os comandos ainda não chegam ao jogo"* (`controller/InputBridge.kt`). O layout (arrastar, escala, reset, salvo por jogo) funciona.
2. **Tela preta × captura.** A janela preta de controles é desenhada sobre o jogo. No **Android 14 QPR2+**, escolhendo **"Um único app"** no diálogo de captura, a TV recebe só o jogo. Com "Tela inteira" (ou Android ≤ 13, que não tem essa opção), a TV mostra a tela de controles. Falta confirmar se o jogo continua renderizando para a captura enquanto está coberto.
3. **Áudio**: só sai na TV se o jogo permitir captura (`allowAudioPlaybackCapture`) e usar `USAGE_GAME`, `USAGE_MEDIA` ou `USAGE_UNKNOWN`. Não há sincronização fina entre áudio e vídeo.
4. **Latência/qualidade**: H.264 sobre TCP sem controle adaptativo de bitrate. Medir na rede real (5 GHz recomendado). O RTT do link aparece no topo da tela de controles.
5. **Rotação**: a captura é sempre em paisagem. Jogos em retrato aparecem com faixas laterais.
6. **Código manual** depende de mDNS/DNS-SD. Alguns roteadores bloqueiam multicast; nesse caso, use o QR.
7. **Leitor de QR** exige Google Play services (o módulo é baixado no primeiro uso).
8. **Decoder da TCL**: confirmar suporte a H.264 1080p60 e ao modo `FEATURE_LowLatency` (Android 11+).
9. Apenas um celular por TV. Assinatura de release não configurada (use os APKs debug para testes).

## Verificação feita nesta entrega

- `protocol`: compilado e **7 testes unitários passando** (framing, pareamento, expiração, bloqueio por tentativas, QR).
- Todo o código Kotlin de `ui`, `mobile` e `tv` (exceto as duas `MainActivity`) passou na **checagem de tipos**:
  - contra o framework Android 15 (jar `android-all` do Robolectric);
  - contra Compose 1.7 (artefatos desktop da JetBrains, mesma API).
  - Nessa checagem, as APIs do AndroidX/Play services foram substituídas por stubs de assinatura.
- **Ainda não executado:** o build Gradle completo com o Android Gradle Plugin (recursos, manifests e geração dos APKs). Rode o comando de build acima e reporte qualquer erro.
