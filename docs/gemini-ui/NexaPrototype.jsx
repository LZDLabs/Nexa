import React, { useState, useEffect, useRef } from 'react';
import {
  Tv,
  Smartphone,
  Wifi,
  QrCode,
  Gamepad2,
  Settings,
  User,
  Power,
  Pause,
  Play,
  Sliders,
  CheckCircle2,
  ChevronRight,
  ChevronLeft,
  ShieldAlert,
  Sparkles,
  Layers,
  Move,
  Maximize2,
  RotateCcw,
  Volume2,
  LogOut,
  Lock,
  ArrowRight,
  Radio,
  Cpu,
  Eye,
  MonitorPlay,
  Compass
} from 'lucide-react';

// ============================================================================
// CUSTOM SVG BRAND MARKS & GAME ARTWORK (NO CYAN/LIGHT BLUE, PURE NEXA PALETTE)
// ============================================================================

const NexaLogo = ({ size = 'md', showTagline = false }) => {
  const dims = size === 'lg' ? 44 : size === 'sm' ? 24 : 32;
  const textClass =
    size === 'lg'
      ? 'text-3xl tracking-[0.22em]'
      : size === 'sm'
      ? 'text-base tracking-[0.18em]'
      : 'text-xl tracking-[0.2em]';

  return (
    <div className="inline-flex items-center gap-3 select-none">
      <svg
        width={dims}
        height={dims}
        viewBox="0 0 48 48"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="shrink-0"
      >
        <rect
          x="2"
          y="2"
          width="44"
          height="44"
          rx="11"
          fill="#161124"
          stroke="#7C3AED"
          strokeWidth="2"
        />
        <path
          d="M14 34V14L27 28.5V14"
          stroke="#F8FAFC"
          strokeWidth="3.2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M34 14V34"
          stroke="#8B5CF6"
          strokeWidth="3.2"
          strokeLinecap="round"
        />
        <circle cx="34" cy="14" r="2.2" fill="#A78BFA" />
      </svg>
      <div className="flex flex-col">
        <span className={`font-bold text-[#F8FAFC] leading-none ${textClass}`}>
          NEXA
        </span>
        {showTagline && (
          <span className="text-[11px] uppercase tracking-[0.24em] text-[#94A3B8] mt-1">
            Console Bridge
          </span>
        )}
      </div>
    </div>
  );
};

// High-contrast crisp QR Code SVG with NEXA center badge
const NexaQrSvg = ({ size = 168 }) => {
  // Deterministic clean QR-like matrix
  const matrix = [
    [1,1,1,1,1,1,1,0,1,0,1,1,0,1,1,1,1,1,1,1],
    [1,0,0,0,0,0,1,0,0,1,0,1,0,1,0,0,0,0,0,1],
    [1,0,1,1,1,0,1,0,1,1,1,0,0,1,0,1,1,1,0,1],
    [1,0,1,1,1,0,1,0,1,0,0,1,0,1,0,1,1,1,0,1],
    [1,0,1,1,1,0,1,0,0,1,1,1,0,1,0,1,1,1,0,1],
    [1,0,0,0,0,0,1,0,1,0,0,0,0,1,0,0,0,0,0,1],
    [1,1,1,1,1,1,1,0,1,0,1,0,1,1,1,1,1,1,1,1],
    [0,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,0,0],
    [1,1,0,1,1,1,1,1,0,0,0,0,1,1,0,1,1,0,1,1],
    [0,1,1,0,1,0,0,0,0,0,0,0,0,1,1,0,0,1,0,1],
    [1,0,1,1,0,1,1,0,0,0,0,0,1,0,1,1,1,0,1,0],
    [1,1,0,0,1,0,0,1,0,0,0,0,0,1,0,0,1,1,0,1],
    [0,0,0,0,0,0,0,0,1,0,1,1,1,0,1,0,1,0,1,1],
    [1,1,1,1,1,1,1,0,1,1,0,0,1,1,0,1,1,1,0,0],
    [1,0,0,0,0,0,1,0,0,1,1,0,0,0,1,0,0,1,1,1],
    [1,0,1,1,1,0,1,0,1,0,1,1,1,1,0,1,1,0,0,1],
    [1,0,1,1,1,0,1,0,1,1,0,0,0,1,1,0,1,1,1,0],
    [1,0,1,1,1,0,1,0,0,0,1,1,0,0,1,1,0,0,1,1],
    [1,0,0,0,0,0,1,0,1,1,0,1,1,1,0,0,1,1,0,1],
    [1,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,0,1,1,1],
  ];

  return (
    <div className="relative inline-flex items-center justify-center p-3.5 bg-[#F8FAFC] rounded-xl shadow-[0_0_40px_rgba(124,58,237,0.22)]">
      <svg width={size} height={size} viewBox="0 0 20 20" shapeRendering="crispEdges">
        {matrix.map((row, rIdx) =>
          row.map((cell, cIdx) => {
            // Leave center 4x4 clear for emblem
            if (rIdx >= 8 && rIdx <= 11 && cIdx >= 8 && cIdx <= 11) return null;
            return cell === 1 ? (
              <rect
                key={`${rIdx}-${cIdx}`}
                x={cIdx}
                y={rIdx}
                width="1"
                height="1"
                fill="#09080D"
              />
            ) : null;
          })
        )}
      </svg>
      {/* Center NEXA emblem */}
      <div className="absolute w-9 h-9 rounded-lg bg-[#161124] border-2 border-[#7C3AED] flex items-center justify-center shadow-md">
        <span className="text-[11px] font-black tracking-tighter text-[#F8FAFC]">NX</span>
      </div>
    </div>
  );
};

// Custom SVG Game Icons
const GameIconSvg = ({ type }) => {
  if (type === 'racing') {
    return (
      <svg viewBox="0 0 48 48" className="w-12 h-12 rounded-xl shrink-0">
        <rect width="48" height="48" rx="12" fill="#1E1433" />
        <path d="M8 34L18 16H30L40 34H8Z" fill="#7C3AED" fillOpacity="0.3" stroke="#A78BFA" strokeWidth="2" />
        <path d="M16 29L24 19L32 29H16Z" fill="#F8FAFC" />
        <circle cx="24" cy="33" r="2.5" fill="#8B5CF6" />
      </svg>
    );
  }
  if (type === 'cyber') {
    return (
      <svg viewBox="0 0 48 48" className="w-12 h-12 rounded-xl shrink-0">
        <rect width="48" height="48" rx="12" fill="#171126" />
        <polygon points="24,9 38,17 38,31 24,39 10,31 10,17" fill="none" stroke="#8B5CF6" strokeWidth="2.2" />
        <circle cx="24" cy="24" r="5" fill="#F8FAFC" />
        <line x1="24" y1="9" x2="24" y2="19" stroke="#A78BFA" strokeWidth="2" />
      </svg>
    );
  }
  if (type === 'fighter') {
    return (
      <svg viewBox="0 0 48 48" className="w-12 h-12 rounded-xl shrink-0">
        <rect width="48" height="48" rx="12" fill="#211538" />
        <path d="M14 34L34 14M14 14L34 34" stroke="#7C3AED" strokeWidth="3.5" strokeLinecap="round" />
        <circle cx="24" cy="24" r="6" fill="#09080D" stroke="#F8FAFC" strokeWidth="2" />
      </svg>
    );
  }
  return (
    <svg viewBox="0 0 48 48" className="w-12 h-12 rounded-xl shrink-0">
      <rect width="48" height="48" rx="12" fill="#181229" />
      <rect x="13" y="13" width="22" height="22" rx="5" stroke="#A78BFA" strokeWidth="2.2" fill="none" />
      <path d="M19 24H29M24 19V29" stroke="#F8FAFC" strokeWidth="2.4" strokeLinecap="round" />
    </svg>
  );
};

// ============================================================================
// SIMULATED TV GAME VIEWPORT (RENDERS ONLY ON TV, NEVER ON MOBILE)
// ============================================================================

const TvGameStreamCanvas = ({ game, activeInput, joystickVec, isPaused }) => {
  const canvasRef = useRef(null);
  const stateRef = useRef({
    offset: 0,
    shipX: 0,
    pulse: 0,
    particles: Array.from({ length: 28 }, (_, i) => ({
      x: (i * 37) % 100,
      y: (i * 53) % 100,
      speed: 0.4 + (i % 4) * 0.25,
    })),
  });

  useEffect(() => {
    let animId;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const render = () => {
      const w = canvas.width;
      const h = canvas.height;
      const st = stateRef.current;

      if (!isPaused) {
        st.offset = (st.offset + 2.4) % 40;
        st.shipX += joystickVec.x * 4.2;
        st.shipX = Math.max(-w * 0.28, Math.min(w * 0.28, st.shipX));
        st.pulse = Math.max(0, st.pulse - 0.04);
      }

      if (activeInput) {
        st.pulse = 1;
      }

      // Deep Obsidian Space / Track Background
      ctx.fillStyle = '#07060B';
      ctx.fillRect(0, 0, w, h);

      // Subtle violet horizon glow
      const horizonY = h * 0.42;
      const grad = ctx.createRadialGradient(w / 2, horizonY, 10, w / 2, horizonY, w * 0.55);
      grad.addColorStop(0, 'rgba(124, 58, 237, 0.28)');
      grad.addColorStop(0.6, 'rgba(35, 25, 59, 0.12)');
      grad.addColorStop(1, 'rgba(7, 6, 11, 0)');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, w, h);

      // Perspective synth-grid floor (strictly violet & dark purple, no cyan)
      ctx.strokeStyle = 'rgba(139, 92, 246, 0.28)';
      ctx.lineWidth = 1;
      for (let i = -8; i <= 8; i++) {
        ctx.beginPath();
        ctx.moveTo(w / 2 + i * 24, horizonY);
        ctx.lineTo(w / 2 + i * 115 - st.shipX * 0.4, h);
        ctx.stroke();
      }
      for (let y = 0; y < h - horizonY; y += 26) {
        const py = horizonY + ((y + st.offset) % (h - horizonY));
        const alpha = ((py - horizonY) / (h - horizonY)) * 0.45;
        ctx.strokeStyle = `rgba(139, 92, 246, ${alpha.toFixed(2)})`;
        ctx.beginPath();
        ctx.moveTo(0, py);
        ctx.lineTo(w, py);
        ctx.stroke();
      }

      // Star / speed particles
      ctx.fillStyle = '#C4B5FD';
      st.particles.forEach((p) => {
        if (!isPaused) {
          p.y = (p.y + p.speed) % 100;
        }
        const px = (p.x / 100) * w;
        const py = (p.y / 100) * horizonY;
        ctx.fillRect(px, py, 2, 2);
      });

      // Player Craft / Vehicle silhouette reacting to phone joystick & buttons
      const cx = w / 2 + st.shipX;
      const cy = h * 0.74 + joystickVec.y * 16;

      // Action button burst ring (A/B/X/Y feedback)
      if (st.pulse > 0.02) {
        ctx.beginPath();
        ctx.arc(cx, cy - 10, 26 + (1 - st.pulse) * 55, 0, Math.PI * 2);
        ctx.strokeStyle = `rgba(167, 139, 250, ${st.pulse * 0.85})`;
        ctx.lineWidth = 3;
        ctx.stroke();
      }

      // Craft body
      ctx.save();
      ctx.translate(cx, cy);
      ctx.rotate(joystickVec.x * 0.18);

      // Engine thrusters (Violet)
      ctx.fillStyle = '#8B5CF6';
      ctx.beginPath();
      ctx.arc(-14, 14, 5 + Math.random() * 2, 0, Math.PI * 2);
      ctx.arc(14, 14, 5 + Math.random() * 2, 0, Math.PI * 2);
      ctx.fill();

      // Sleek hull
      ctx.fillStyle = '#161124';
      ctx.strokeStyle = '#F8FAFC';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(0, -28);
      ctx.lineTo(34, 14);
      ctx.lineTo(14, 18);
      ctx.lineTo(0, 10);
      ctx.lineTo(-14, 18);
      ctx.lineTo(-34, 14);
      ctx.closePath();
      ctx.fill();
      ctx.stroke();

      // Cockpit canopy
      ctx.fillStyle = '#7C3AED';
      ctx.beginPath();
      ctx.moveTo(0, -14);
      ctx.lineTo(8, 4);
      ctx.lineTo(-8, 4);
      ctx.closePath();
      ctx.fill();
      ctx.restore();

      // In-game minimal telemetry HUD (part of the streamed game video)
      ctx.fillStyle = 'rgba(248, 250, 252, 0.85)';
      ctx.font = '600 11px monospace';
      ctx.fillText(`JOGO: ${game?.title?.toUpperCase() || 'AETHER DRIFT'}`, 18, h - 34);
      ctx.fillStyle = '#A78BFA';
      ctx.fillText(
        `VEL: ${isPaused ? '0' : Math.round(284 + Math.abs(joystickVec.y) * 60)} KM/H  •  VOLTA 2/3`,
        18,
        h - 18
      );

      if (activeInput) {
        ctx.fillStyle = 'rgba(124, 58, 237, 0.9)';
        ctx.fillRect(w - 145, h - 36, 127, 22);
        ctx.fillStyle = '#F8FAFC';
        ctx.fillText(`INPUT: BOTÃO [${activeInput}]`, w - 137, h - 21);
      }

      animId = requestAnimationFrame(render);
    };

    animId = requestAnimationFrame(render);
    return () => cancelAnimationFrame(animId);
  }, [game, activeInput, joystickVec, isPaused]);

  return (
    <canvas
      ref={canvasRef}
      width={640}
      height={360}
      className="w-full h-full object-cover block"
    />
  );
};

// ============================================================================
// MAIN APPLICATION SHOWCASE
// ============================================================================

export default function App() {
  // Showcase Navigation Mode
  const [viewMode, setViewMode] = useState('split'); // 'split' | 'tv' | 'mobile' | 'specs'

  // Shared Cross-Device Session State
  const [tvScreen, setTvScreen] = useState('home'); // 'home' | 'pairing' | 'streaming'
  const [tvFocusIndex, setTvFocusIndex] = useState(0); // D-pad simulation on TV
  const [mobileScreen, setMobileScreen] = useState('auth'); // 'auth' | 'home' | 'connect' | 'games' | 'controller'
  const [authMode, setAuthMode] = useState('login'); // 'login' | 'register'
  const [username, setUsername] = useState('lucas.gamer');
  const [isPaired, setIsPaired] = useState(false);
  const [manualCode, setManualCode] = useState('');
  const [selectedGame, setSelectedGame] = useState(null);
  const [showCaptureModal, setShowCaptureModal] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [showTvMenuOverlay, setShowTvMenuOverlay] = useState(false);

  // Virtual Controller Interactive & Customization State
  const [activeBtn, setActiveBtn] = useState(null);
  const [joystickVec, setJoystickVec] = useState({ x: 0, y: 0 });
  const [isCustomizing, setIsCustomizing] = useState(false);
  const [selectedControlId, setSelectedControlId] = useState('btnA');
  const [buttonScale, setButtonScale] = useState(1);
  const [controlsConfig, setControlsConfig] = useState({
    joystick: { x: 18, y: 58, size: 1, label: 'Analógico' },
    btnA: { x: 82, y: 72, size: 1, label: 'A', action: 'Acelerar / Confirmar' },
    btnB: { x: 91, y: 56, size: 1, label: 'B', action: 'Frear / Voltar' },
    btnX: { x: 73, y: 56, size: 1, label: 'X', action: 'Turbo / Ação 1' },
    btnY: { x: 82, y: 40, size: 1, label: 'Y', action: 'Câmera / Ação 2' },
  });

  const controllerPadRef = useRef(null);
  const draggingControlRef = useRef(null);
  const draggingJoyRef = useRef(false);

  // Compatible Android Games List
  const installedGames = [
    {
      id: 'aether',
      title: 'Aether Drift: Eclipse',
      genre: 'Corrida Sci-Fi • 60 FPS',
      profile: 'Perfil Corrida Pro',
      icon: 'racing',
      latency: '4ms',
    },
    {
      id: 'obsidian',
      title: 'Obsidian Protocol',
      genre: 'Ação Tática • Suporte Nativo',
      profile: 'Perfil FPS/Ação',
      icon: 'cyber',
      latency: '5ms',
    },
    {
      id: 'valkyrie',
      title: 'Shadow Arena NEXA',
      genre: 'Luta Arcade • Zero Delay',
      profile: 'Perfil Arcade 6-Botões',
      icon: 'fighter',
      latency: '4ms',
    },
    {
      id: 'chronos',
      title: 'Chronos Hollow',
      genre: 'Metroidvania • 1080p HDR',
      profile: 'Perfil Plataforma',
      icon: 'platformer',
      latency: '5ms',
    },
  ];

  // 8-Step UX Flow Definition
  const uxSteps = [
    {
      step: 1,
      title: 'Abrir NEXA TV',
      desc: 'TV exibe QR Code e código local.',
      apply: () => {
        setIsPaired(false);
        setSelectedGame(null);
        setTvScreen('home');
        setMobileScreen('auth');
      },
    },
    {
      step: 2,
      title: 'Login Mobile',
      desc: 'Acesso rápido na conta NEXA.',
      apply: () => {
        setTvScreen('home');
        setMobileScreen('auth');
      },
    },
    {
      step: 3,
      title: 'Escanear QR Code',
      desc: 'Pareamento via Wi-Fi 5GHz.',
      apply: () => {
        setTvScreen('home');
        setMobileScreen('connect');
      },
    },
    {
      step: 4,
      title: 'TV Pareada',
      desc: 'Conexão direta estabelecida.',
      apply: () => {
        setIsPaired(true);
        setTvScreen('pairing');
        setMobileScreen('connect');
      },
    },
    {
      step: 5,
      title: 'Meus Jogos',
      desc: 'Lista de títulos compatíveis.',
      apply: () => {
        setIsPaired(true);
        setTvScreen('pairing');
        setMobileScreen('games');
      },
    },
    {
      step: 6,
      title: 'Autorizar Captura',
      desc: 'Permissão de áudio e vídeo.',
      apply: () => {
        setIsPaired(true);
        setSelectedGame(installedGames[0]);
        setTvScreen('pairing');
        setMobileScreen('games');
        setShowCaptureModal(true);
      },
    },
    {
      step: 7,
      title: 'Jogo na TV',
      desc: 'Vídeo transmitido em tela cheia.',
      apply: () => {
        setIsPaired(true);
        setSelectedGame(installedGames[0]);
        setShowCaptureModal(false);
        setIsPaused(false);
        setTvScreen('streaming');
        setMobileScreen('controller');
      },
    },
    {
      step: 8,
      title: 'Controle OLED Preto',
      desc: 'Celular exibe apenas controles.',
      apply: () => {
        setIsPaired(true);
        setSelectedGame(installedGames[0]);
        setShowCaptureModal(false);
        setTvScreen('streaming');
        setMobileScreen('controller');
      },
    },
  ];

  // Determine active step for stepper highlight
  const getCurrentStep = () => {
    if (tvScreen === 'streaming' && mobileScreen === 'controller') return 8;
    if (showCaptureModal) return 6;
    if (mobileScreen === 'games' && isPaired) return 5;
    if (isPaired && tvScreen === 'pairing') return 4;
    if (mobileScreen === 'connect') return 3;
    if (mobileScreen === 'home') return 2;
    return 1;
  };

  // Pairing Action Handler
  const handleCompletePairing = () => {
    setIsPaired(true);
    setTvScreen('pairing');
  };

  // Disconnect Handler
  const handleDisconnect = () => {
    setIsPaired(false);
    setSelectedGame(null);
    setIsPaused(false);
    setShowTvMenuOverlay(false);
    setTvScreen('home');
    setMobileScreen('home');
  };

  // Launch Game Flow
  const handleSelectGameToPlay = (game) => {
    setSelectedGame(game);
    if (!isPaired) {
      setMobileScreen('connect');
      return;
    }
    setShowCaptureModal(true);
  };

  const handleAuthorizeAndStart = () => {
    setShowCaptureModal(false);
    setIsPaused(false);
    setTvScreen('streaming');
    setMobileScreen('controller');
  };

  // Trigger virtual button press with visual feedback on TV
  const triggerVirtualButton = (label) => {
    if (isCustomizing) return;
    setActiveBtn(label);
    setTimeout(() => setActiveBtn(null), 260);
  };

  // Pointer handlers for Virtual Joystick & Control Layout Dragging
  const handlePadPointerMove = (e) => {
    if (!controllerPadRef.current) return;
    const rect = controllerPadRef.current.getBoundingClientRect();

    // Customizing layout: drag selected control
    if (isCustomizing && draggingControlRef.current) {
      const id = draggingControlRef.current;
      const relX = Math.max(10, Math.min(92, ((e.clientX - rect.left) / rect.width) * 100));
      const relY = Math.max(22, Math.min(86, ((e.clientY - rect.top) / rect.height) * 100));
      setControlsConfig((prev) => ({
        ...prev,
        [id]: { ...prev[id], x: relX, y: relY },
      }));
      return;
    }

    // Gameplay mode: virtual joystick movement
    if (!isCustomizing && draggingJoyRef.current) {
      const joyPos = controlsConfig.joystick;
      const centerX = rect.left + (joyPos.x / 100) * rect.width;
      const centerY = rect.top + (joyPos.y / 100) * rect.height;
      const maxRadius = 36;
      const dx = e.clientX - centerX;
      const dy = e.clientY - centerY;
      const dist = Math.hypot(dx, dy);
      const clamped = Math.min(dist, maxRadius);
      const angle = Math.atan2(dy, dx);
      setJoystickVec({
        x: (Math.cos(angle) * clamped) / maxRadius,
        y: (Math.sin(angle) * clamped) / maxRadius,
      });
    }
  };

  const handlePadPointerUp = () => {
    draggingControlRef.current = null;
    if (draggingJoyRef.current) {
      draggingJoyRef.current = false;
      setJoystickVec({ x: 0, y: 0 });
    }
  };

  const updateSelectedScale = (newScale) => {
    setButtonScale(newScale);
    setControlsConfig((prev) => ({
      ...prev,
      [selectedControlId]: {
        ...prev[selectedControlId],
        size: newScale,
      },
    }));
  };

  const resetControlsLayout = () => {
    setControlsConfig({
      joystick: { x: 18, y: 58, size: 1, label: 'Analógico' },
      btnA: { x: 82, y: 72, size: 1, label: 'A', action: 'Acelerar / Confirmar' },
      btnB: { x: 91, y: 56, size: 1, label: 'B', action: 'Frear / Voltar' },
      btnX: { x: 73, y: 56, size: 1, label: 'X', action: 'Turbo / Ação 1' },
      btnY: { x: 82, y: 40, size: 1, label: 'Y', action: 'Câmera / Ação 2' },
    });
    setButtonScale(1);
  };

  // ==========================================================================
  // RENDER: APP 1 — NEXA TV (ANDROID TV / GOOGLE TV 16:9 VIEWPORT)
  // ==========================================================================
  const renderNexaTv = () => {
    return (
      <div className="flex flex-col w-full">
        {/* TV Device Header Bar */}
        <div className="flex items-center justify-between mb-3 px-1">
          <div className="flex items-center gap-2.5">
            <span className="inline-flex items-center justify-center w-7 h-7 rounded-lg bg-[#161124] border border-[#7C3AED]/40 text-[#A78BFA]">
              <Tv className="w-4 h-4" />
            </span>
            <div>
              <h2 className="text-sm font-semibold text-[#F8FAFC] tracking-wide">
                APLICATIVO 1 — NEXA TV
              </h2>
              <p className="text-xs text-[#94A3B8]">
                Android TV / Google TV • Navegação D-Pad & Distância 3m
              </p>
            </div>
          </div>

          {/* Quick TV Screen Switcher & Simulated Remote D-Pad */}
          <div className="flex items-center gap-1.5 bg-[#09080D] p-1 rounded-lg border border-[#23193B]">
            {[
              { id: 'home', label: 'Inicial' },
              { id: 'pairing', label: 'Conexão' },
              { id: 'streaming', label: 'Transmissão' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  if (tab.id === 'streaming' && !selectedGame) {
                    setSelectedGame(installedGames[0]);
                  }
                  setTvScreen(tab.id);
                }}
                className={`px-2.5 py-1 rounded-md text-xs font-medium transition-all ${
                  tvScreen === tab.id
                    ? 'bg-[#7C3AED] text-[#F8FAFC] shadow-sm'
                    : 'text-[#94A3B8] hover:text-[#F8FAFC]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* 16:9 Android TV Bezel Frame */}
        <div className="relative w-full aspect-video bg-[#000000] rounded-2xl p-2.5 sm:p-3 border-2 border-[#23193B] shadow-[0_24px_60px_rgba(0,0,0,0.85)] overflow-hidden">
          <div className="relative w-full h-full bg-[#09080D] rounded-xl overflow-hidden flex flex-col justify-between border border-[#161124]">
            {/* Ambient subtle dark purple radial light (only on non-streaming screens) */}
            {tvScreen !== 'streaming' && (
              <div
                className="pointer-events-none absolute inset-0"
                style={{
                  background:
                    'radial-gradient(circle at 50% 0%, rgba(124, 58, 237, 0.16) 0%, rgba(9, 8, 13, 0) 65%)',
                }}
              />
            )}

            {/* ---------------- TV SCREEN 1: TELA INICIAL ---------------- */}
            {tvScreen === 'home' && (
              <div className="relative z-10 flex flex-col justify-between h-full p-5 sm:p-8">
                {/* Top TV Status Bar */}
                <div className="flex items-center justify-between">
                  <NexaLogo size="sm" />
                  <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#161124] border border-[#23193B] text-xs text-[#F8FAFC]">
                    <Wifi className="w-3.5 h-3.5 text-[#8B5CF6]" />
                    <span>Wi-Fi Local 5GHz • Pronto</span>
                  </div>
                </div>

                {/* Main 2-Column 10-foot TV Layout */}
                <div className="grid grid-cols-12 gap-6 items-center my-auto py-2">
                  {/* Left: Welcome & D-Pad Action */}
                  <div className="col-span-7 flex flex-col items-start text-left space-y-3 sm:space-y-4">
                    <span className="inline-block text-[11px] font-semibold uppercase tracking-[0.2em] text-[#A78BFA] bg-[#161124] px-2.5 py-1 rounded-md border border-[#7C3AED]/30">
                      Receptor Android TV
                    </span>
                    <h1 className="text-xl sm:text-2xl lg:text-3xl font-bold text-[#F8FAFC] leading-tight">
                      Pronto para jogar na tela grande.
                    </h1>
                    <p className="text-xs sm:text-sm text-[#94A3B8] max-w-md leading-relaxed">
                      Conecte seu smartphone Android na mesma rede Wi-Fi para transmitir seus jogos com latência ultra-baixa.
                    </p>

                    {/* D-Pad Focusable Primary Button */}
                    <div className="pt-1 flex flex-wrap items-center gap-3">
                      <button
                        onClick={() => {
                          setTvFocusIndex(0);
                          setTvScreen('pairing');
                        }}
                        className={`group flex items-center gap-2.5 px-5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all ${
                          tvFocusIndex === 0
                            ? 'bg-[#7C3AED] text-[#F8FAFC] ring-4 ring-[#8B5CF6]/45 scale-[1.02]'
                            : 'bg-[#161124] text-[#F8FAFC] border border-[#23193B]'
                        }`}
                      >
                        <Radio className="w-4 h-4" />
                        <span>Iniciar Conexão</span>
                      </button>

                      <button
                        onClick={() => setTvFocusIndex(1)}
                        className={`px-4 py-2.5 rounded-xl text-xs font-medium transition-all ${
                          tvFocusIndex === 1
                            ? 'bg-[#23193B] text-[#F8FAFC] ring-4 ring-[#8B5CF6]/45'
                            : 'bg-[#161124]/80 text-[#94A3B8] border border-[#23193B]'
                        }`}
                      >
                        Sala de Estar • TV
                      </button>
                    </div>

                    {/* Concise 3-step instructions */}
                    <div className="grid grid-cols-3 gap-2 pt-2 w-full max-w-md">
                      {[
                        '1. Abra o NEXA no celular',
                        '2. Escaneie o QR Code ao lado',
                        '3. Escolha o jogo e jogue',
                      ].map((inst, idx) => (
                        <div
                          key={idx}
                          className="bg-[#161124]/75 border border-[#23193B] rounded-lg px-2.5 py-2 text-[11px] text-[#94A3B8] leading-snug"
                        >
                          {inst}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Right: Large Centered QR Code + Alternative Code */}
                  <div className="col-span-5 flex flex-col items-center justify-center bg-[#161124]/90 border border-[#23193B] rounded-2xl p-4 sm:p-5">
                    <NexaQrSvg size={136} />
                    <div className="mt-3 text-center">
                      <span className="block text-[10px] uppercase tracking-[0.2em] text-[#94A3B8]">
                        Código Alternativo
                      </span>
                      <span className="font-mono text-base sm:text-lg font-bold tracking-[0.24em] text-[#F8FAFC] bg-[#09080D] px-3.5 py-1 rounded-lg border border-[#7C3AED]/40 mt-1 inline-block">
                        NX-8492
                      </span>
                    </div>
                  </div>
                </div>

                {/* Bottom D-Pad Hint */}
                <div className="flex items-center justify-between text-[11px] text-[#94A3B8] border-t border-[#161124] pt-2.5">
                  <span>Use o controle remoto (D-Pad) para navegar</span>
                  <span className="font-mono text-[#A78BFA]">IP Local: 192.168.1.42</span>
                </div>
              </div>
            )}

            {/* ---------------- TV SCREEN 2: TELA DE CONEXÃO ---------------- */}
            {tvScreen === 'pairing' && (
              <div className="relative z-10 flex flex-col justify-between h-full p-6 sm:p-8">
                <div className="flex items-center justify-between">
                  <NexaLogo size="sm" />
                  <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#161124] border border-[#7C3AED]/40 text-xs text-[#F8FAFC]">
                    <span
                      className={`w-2 h-2 rounded-full ${
                        isPaired ? 'bg-[#8B5CF6]' : 'bg-[#A78BFA] animate-ping'
                      }`}
                    />
                    <span>
                      {isPaired
                        ? 'Conectado via Wi-Fi Local • 4ms'
                        : 'Aguardando pareamento do celular...'}
                    </span>
                  </div>
                </div>

                {/* Center Radar / Connected Device Card */}
                <div className="flex flex-col items-center justify-center my-auto text-center">
                  <div className="relative flex items-center justify-center w-28 h-28 mb-4">
                    {/* Concentric Violet Radar Rings */}
                    <span className="absolute inset-0 rounded-full border border-[#7C3AED]/30 animate-ping" />
                    <span className="absolute inset-3 rounded-full border border-[#8B5CF6]/45" />
                    <div className="relative w-16 h-16 rounded-2xl bg-[#161124] border-2 border-[#7C3AED] flex items-center justify-center shadow-[0_0_30px_rgba(124,58,237,0.35)]">
                      <Smartphone className="w-8 h-8 text-[#F8FAFC]" />
                    </div>
                  </div>

                  {isPaired ? (
                    <>
                      <span className="text-xs uppercase tracking-[0.2em] text-[#A78BFA] font-semibold">
                        Dispositivo Conectado
                      </span>
                      <h2 className="text-2xl font-bold text-[#F8FAFC] mt-1">
                        Pixel 9 Pro de Lucas
                      </h2>
                      <p className="text-xs sm:text-sm text-[#94A3B8] mt-1.5 max-w-md">
                        Selecione um jogo em <strong className="text-[#F8FAFC]">Meus Jogos</strong> no seu celular para iniciar a transmissão automaticamente.
                      </p>
                    </>
                  ) : (
                    <>
                      <span className="text-xs uppercase tracking-[0.2em] text-[#94A3B8] font-semibold">
                        Procurando Celular
                      </span>
                      <h2 className="text-xl sm:text-2xl font-bold text-[#F8FAFC] mt-1">
                        Código de Sessão: <span className="font-mono text-[#A78BFA]">NX-8492</span>
                      </h2>
                      <p className="text-xs text-[#94A3B8] mt-1.5">
                        Escaneie o QR Code ou digite o código no aplicativo NEXA Mobile.
                      </p>
                    </>
                  )}

                  {/* Remote-focused Disconnect Button */}
                  <div className="mt-6 flex items-center gap-3">
                    {!isPaired && (
                      <button
                        onClick={handleCompletePairing}
                        className="px-4 py-2 rounded-xl bg-[#7C3AED] text-[#F8FAFC] text-xs font-semibold ring-2 ring-[#8B5CF6]/50 hover:bg-[#8B5CF6] transition"
                      >
                        Simular Pareamento Recebido
                      </button>
                    )}
                    <button
                      onClick={handleDisconnect}
                      className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#161124] hover:bg-[#23193B] text-[#F8FAFC] text-xs font-semibold border border-[#23193B] ring-4 ring-[#8B5CF6]/40 transition"
                    >
                      <Power className="w-3.5 h-3.5 text-[#A78BFA]" />
                      <span>Desconectar</span>
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between text-[11px] text-[#94A3B8] border-t border-[#161124] pt-2.5">
                  <span>Codec: HEVC 1080p60 • Áudio PCM Estéreo</span>
                  <span>Modo Baixa Latência Ativo</span>
                </div>
              </div>
            )}

            {/* ---------------- TV SCREEN 3: TELA DE TRANSMISSÃO ---------------- */}
            {tvScreen === 'streaming' && (
              <div
                className="relative w-full h-full bg-[#000000] select-none cursor-pointer"
                onClick={() => setShowTvMenuOverlay((prev) => !prev)}
              >
                {/* Fullscreen Game Stream */}
                <TvGameStreamCanvas
                  game={selectedGame || installedGames[0]}
                  activeInput={activeBtn}
                  joystickVec={joystickVec}
                  isPaused={isPaused}
                />

                {/* Minimal Top-Right Connection Pill (Unobtrusive) */}
                <div className="absolute top-3.5 right-3.5 flex items-center gap-2 px-3 py-1 rounded-full bg-[#09080D]/80 backdrop-blur-md border border-[#7C3AED]/40 text-[11px] font-medium text-[#F8FAFC]">
                  <span className="w-2 h-2 rounded-full bg-[#8B5CF6]" />
                  <span>NEXA • 1080p 60fps • 5ms</span>
                </div>

                {/* Paused State Banner */}
                {isPaused && (
                  <div className="absolute inset-0 bg-[#000000]/70 backdrop-blur-xs flex flex-col items-center justify-center">
                    <span className="px-3 py-1 rounded-md bg-[#161124] border border-[#7C3AED]/40 text-xs uppercase tracking-[0.2em] text-[#A78BFA] mb-2">
                      Sessão Pausada
                    </span>
                    <h3 className="text-xl font-bold text-[#F8FAFC]">
                      Jogo pausado pelo controle
                    </h3>
                  </div>
                )}

                {/* Subtle Remote-Triggered Overlay to End Stream */}
                <div
                  className={`absolute bottom-3.5 right-3.5 transition-opacity duration-300 ${
                    showTvMenuOverlay ? 'opacity-100' : 'opacity-75 hover:opacity-100'
                  }`}
                >
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDisconnect();
                    }}
                    className="flex items-center gap-2 px-3.5 py-1.5 rounded-lg bg-[#09080D]/85 hover:bg-[#161124] text-[#F8FAFC] text-xs font-medium border border-[#23193B] hover:border-[#7C3AED] transition"
                  >
                    <Power className="w-3.5 h-3.5 text-[#A78BFA]" />
                    <span>Encerrar Transmissão</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* TV Bezel Stand & Remote Hint */}
        <div className="flex flex-col items-center mt-2">
          <div className="w-24 h-1.5 bg-[#23193B] rounded-full" />
          <div className="flex items-center gap-4 mt-2 text-[11px] text-[#94A3B8]">
            <span className="flex items-center gap-1">
              <Compass className="w-3.5 h-3.5 text-[#8B5CF6]" /> Foco D-Pad Ativo
            </span>
            <span>•</span>
            <span>Proporção 16:9 Widescreen</span>
          </div>
        </div>
      </div>
    );
  };

  // ==========================================================================
  // RENDER: APP 2 — NEXA MOBILE (ANDROID PHONE TOUCH & OLED CONTROLLER)
  // ==========================================================================
  const renderNexaMobile = () => {
    const isControllerScreen = mobileScreen === 'controller';

    return (
      <div className="flex flex-col items-center w-full">
        {/* Mobile Device Header Bar */}
        <div className="flex items-center justify-between w-full mb-3 px-1">
          <div className="flex items-center gap-2.5">
            <span className="inline-flex items-center justify-center w-7 h-7 rounded-lg bg-[#161124] border border-[#7C3AED]/40 text-[#A78BFA]">
              <Smartphone className="w-4 h-4" />
            </span>
            <div>
              <h2 className="text-sm font-semibold text-[#F8FAFC] tracking-wide">
                APLICATIVO 2 — NEXA MOBILE
              </h2>
              <p className="text-xs text-[#94A3B8]">
                Android • Toque & Controle OLED Preto
              </p>
            </div>
          </div>

          {/* Mobile Screen Quick Selector */}
          <div className="flex flex-wrap items-center gap-1 bg-[#09080D] p-1 rounded-lg border border-[#23193B]">
            {[
              { id: 'auth', label: 'Login' },
              { id: 'home', label: 'Início' },
              { id: 'connect', label: 'Conexão' },
              { id: 'games', label: 'Jogos' },
              { id: 'controller', label: 'Controle' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  if (tab.id === 'controller') {
                    setIsPaired(true);
                    if (!selectedGame) setSelectedGame(installedGames[0]);
                    setTvScreen('streaming');
                  }
                  setMobileScreen(tab.id);
                }}
                className={`px-2 py-1 rounded-md text-[11px] font-medium transition-all ${
                  mobileScreen === tab.id
                    ? 'bg-[#7C3AED] text-[#F8FAFC]'
                    : 'text-[#94A3B8] hover:text-[#F8FAFC]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* Android Smartphone Bezel Frame (Adapts smoothly to Landscape OLED Controller) */}
        <div
          className={`relative transition-all duration-500 bg-[#050507] border-[3px] border-[#23193B] shadow-[0_24px_60px_rgba(0,0,0,0.9)] overflow-hidden ${
            isControllerScreen
              ? 'w-full max-w-[540px] aspect-[19.5/9.5] rounded-[28px] p-2.5'
              : 'w-[310px] sm:w-[330px] h-[610px] rounded-[34px] p-2.5'
          }`}
        >
          {/* Inner Android Screen */}
          <div
            className={`relative w-full h-full rounded-[24px] overflow-hidden flex flex-col ${
              isControllerScreen ? 'bg-[#000000]' : 'bg-[#09080D]'
            }`}
          >
            {/* Android Status Bar (Hidden during pure-black controller mode or ultra-minimal) */}
            {!isControllerScreen && (
              <div className="flex items-center justify-between px-5 pt-2.5 pb-1.5 text-[10px] text-[#94A3B8] select-none">
                <span className="font-mono font-semibold text-[#F8FAFC]">09:41</span>
                <div className="w-16 h-3.5 bg-[#000000] rounded-full border border-[#161124]" />
                <div className="flex items-center gap-1.5">
                  <Wifi className="w-3 h-3 text-[#8B5CF6]" />
                  <span>5G</span>
                </div>
              </div>
            )}

            {/* ---------------- MOBILE SCREEN 1: AUTENTICAÇÃO ---------------- */}
            {mobileScreen === 'auth' && (
              <div className="flex flex-col justify-between h-full px-6 py-6">
                <div className="mt-4 flex flex-col items-center text-center">
                  <NexaLogo size="md" showTagline />
                  <p className="text-xs text-[#94A3B8] mt-4">
                    Seu smartphone como console portátil na TV.
                  </p>
                </div>

                {/* Auth Form */}
                <div className="space-y-3.5 my-auto">
                  {/* Toggle Entrar / Criar Conta */}
                  <div className="grid grid-cols-2 p-1 bg-[#161124] rounded-xl border border-[#23193B]">
                    <button
                      onClick={() => setAuthMode('login')}
                      className={`py-1.5 text-xs font-semibold rounded-lg transition ${
                        authMode === 'login'
                          ? 'bg-[#7C3AED] text-[#F8FAFC]'
                          : 'text-[#94A3B8]'
                      }`}
                    >
                      Entrar
                    </button>
                    <button
                      onClick={() => setAuthMode('register')}
                      className={`py-1.5 text-xs font-semibold rounded-lg transition ${
                        authMode === 'register'
                          ? 'bg-[#7C3AED] text-[#F8FAFC]'
                          : 'text-[#94A3B8]'
                      }`}
                    >
                      Criar Conta
                    </button>
                  </div>

                  <div>
                    <label className="block text-[11px] font-medium text-[#94A3B8] mb-1">
                      Usuário (Username)
                    </label>
                    <div className="flex items-center gap-2 bg-[#161124] border border-[#23193B] focus-within:border-[#7C3AED] rounded-xl px-3 py-2.5">
                      <User className="w-4 h-4 text-[#8B5CF6]" />
                      <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="bg-transparent text-xs text-[#F8FAFC] w-full focus:outline-none"
                        placeholder="seu.usuario"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] font-medium text-[#94A3B8] mb-1">
                      Senha
                    </label>
                    <div className="flex items-center gap-2 bg-[#161124] border border-[#23193B] focus-within:border-[#7C3AED] rounded-xl px-3 py-2.5">
                      <Lock className="w-4 h-4 text-[#8B5CF6]" />
                      <input
                        type="password"
                        defaultValue="••••••••••••"
                        className="bg-transparent text-xs text-[#F8FAFC] w-full focus:outline-none"
                      />
                    </div>
                  </div>

                  <button
                    onClick={() => setMobileScreen('home')}
                    className="w-full py-3 rounded-xl bg-[#7C3AED] hover:bg-[#8B5CF6] text-[#F8FAFC] text-xs font-semibold tracking-wide transition shadow-[0_8px_24px_rgba(124,58,237,0.35)] flex items-center justify-center gap-2"
                  >
                    <span>{authMode === 'login' ? 'Entrar no NEXA' : 'Cadastrar Conta'}</span>
                    <ArrowRight className="w-4 h-4" />
                  </button>
                </div>

                <p className="text-center text-[11px] text-[#94A3B8]">
                  Sincronização local criptografada • v2.4
                </p>
              </div>
            )}

            {/* ---------------- MOBILE SCREEN 2: TELA PRINCIPAL ---------------- */}
            {mobileScreen === 'home' && (
              <div className="flex flex-col justify-between h-full px-5 py-4">
                {/* Top Greeting & Profile */}
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] text-[#94A3B8]">Bem-vindo de volta,</span>
                    <h3 className="text-lg font-bold text-[#F8FAFC]">
                      Olá, {username.split('.')[0] || 'Lucas'}
                    </h3>
                  </div>
                  <button
                    onClick={() => setMobileScreen('auth')}
                    title="Perfil"
                    className="w-9 h-9 rounded-xl bg-[#161124] border border-[#23193B] flex items-center justify-center text-[#A78BFA] hover:border-[#7C3AED]"
                  >
                    <User className="w-4 h-4" />
                  </button>
                </div>

                {/* Connection Status & Primary CTA Card */}
                <div className="bg-[#161124] border border-[#23193B] rounded-2xl p-4 space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-[#A78BFA]">
                      Status da TV
                    </span>
                    <span
                      className={`inline-flex items-center gap-1.5 text-[11px] px-2 py-0.5 rounded-full ${
                        isPaired
                          ? 'bg-[#7C3AED]/20 text-[#F8FAFC] border border-[#7C3AED]/40'
                          : 'bg-[#09080D] text-[#94A3B8]'
                      }`}
                    >
                      <span
                        className={`w-1.5 h-1.5 rounded-full ${
                          isPaired ? 'bg-[#8B5CF6]' : 'bg-[#94A3B8]'
                        }`}
                      />
                      {isPaired ? 'TV Conectada' : 'Desconectado'}
                    </span>
                  </div>

                  <div>
                    <h4 className="text-sm font-bold text-[#F8FAFC]">
                      {isPaired ? 'Sala de Estar • Sony Bravia XR' : 'Nenhuma TV pareada'}
                    </h4>
                    <p className="text-xs text-[#94A3B8] mt-0.5">
                      {isPaired
                        ? 'Pronto para transmitir jogos em 1080p 60fps.'
                        : 'Escaneie o QR Code exibido no aplicativo NEXA TV.'}
                    </p>
                  </div>

                  <button
                    onClick={() => setMobileScreen('connect')}
                    className="w-full py-2.5 rounded-xl bg-[#7C3AED] hover:bg-[#8B5CF6] text-[#F8FAFC] text-xs font-semibold flex items-center justify-center gap-2 transition"
                  >
                    <QrCode className="w-4 h-4" />
                    <span>{isPaired ? 'Gerenciar Conexão TV' : 'Conectar à TV'}</span>
                  </button>
                </div>

                {/* Meus Jogos Action Card */}
                <button
                  onClick={() => setMobileScreen('games')}
                  className="w-full text-left bg-[#161124] hover:bg-[#23193B]/70 border border-[#23193B] hover:border-[#7C3AED]/60 rounded-2xl p-4 transition flex items-center justify-between group"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-11 h-11 rounded-xl bg-[#09080D] border border-[#7C3AED]/40 flex items-center justify-center text-[#8B5CF6]">
                      <Gamepad2 className="w-5 h-5" />
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-[#F8FAFC]">Meus Jogos</h4>
                      <p className="text-xs text-[#94A3B8]">
                        {installedGames.length} jogos instalados compatíveis
                      </p>
                    </div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-[#94A3B8] group-hover:text-[#F8FAFC] transition" />
                </button>

                {/* Bottom Quick Bar: Configurações & Perfil */}
                <div className="grid grid-cols-2 gap-2.5 pt-1">
                  <button
                    onClick={() => setMobileScreen('games')}
                    className="flex items-center justify-center gap-2 py-2.5 rounded-xl bg-[#161124] border border-[#23193B] text-xs text-[#94A3B8] hover:text-[#F8FAFC]"
                  >
                    <Settings className="w-3.5 h-3.5 text-[#8B5CF6]" />
                    <span>Configurações</span>
                  </button>
                  <button
                    onClick={() => setMobileScreen('auth')}
                    className="flex items-center justify-center gap-2 py-2.5 rounded-xl bg-[#161124] border border-[#23193B] text-xs text-[#94A3B8] hover:text-[#F8FAFC]"
                  >
                    <User className="w-3.5 h-3.5 text-[#8B5CF6]" />
                    <span>Perfil</span>
                  </button>
                </div>
              </div>
            )}

            {/* ---------------- MOBILE SCREEN 3: CONEXÃO (QR & MANUAL) ---------------- */}
            {mobileScreen === 'connect' && (
              <div className="flex flex-col justify-between h-full px-5 py-4">
                <div className="flex items-center justify-between">
                  <button
                    onClick={() => setMobileScreen('home')}
                    className="p-1.5 rounded-lg bg-[#161124] text-[#94A3B8] hover:text-[#F8FAFC]"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <span className="text-xs font-bold text-[#F8FAFC]">Conectar à TV</span>
                  <div className="w-7" />
                </div>

                {/* Simulated QR Code Camera Viewfinder */}
                <div className="relative flex flex-col items-center justify-center bg-[#050507] border border-[#23193B] rounded-2xl p-4 my-2">
                  <div className="relative w-36 h-36 flex items-center justify-center">
                    {/* Violet Viewfinder Corner Brackets */}
                    <span className="absolute top-0 left-0 w-5 h-5 border-t-2 border-l-2 border-[#8B5CF6] rounded-tl-md" />
                    <span className="absolute top-0 right-0 w-5 h-5 border-t-2 border-r-2 border-[#8B5CF6] rounded-tr-md" />
                    <span className="absolute bottom-0 left-0 w-5 h-5 border-b-2 border-l-2 border-[#8B5CF6] rounded-bl-md" />
                    <span className="absolute bottom-0 right-0 w-5 h-5 border-b-2 border-r-2 border-[#8B5CF6] rounded-br-md" />

                    {/* Scanline */}
                    <div className="w-28 h-0.5 bg-[#7C3AED] shadow-[0_0_12px_#8B5CF6] animate-pulse" />
                  </div>

                  <button
                    onClick={handleCompletePairing}
                    className="mt-3 px-3.5 py-1.5 rounded-lg bg-[#7C3AED] hover:bg-[#8B5CF6] text-[#F8FAFC] text-[11px] font-semibold transition"
                  >
                    {isPaired ? 'QR Code Validado ✓' : 'Simular Leitura de QR Code'}
                  </button>
                </div>

                {/* Manual Code Input */}
                <div className="space-y-2">
                  <label className="block text-[11px] text-[#94A3B8]">
                    Ou insira o código exibido na TV:
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={manualCode}
                      onChange={(e) => setManualCode(e.target.value.toUpperCase())}
                      placeholder="NX-8492"
                      maxLength={7}
                      className="flex-1 bg-[#161124] border border-[#23193B] focus:border-[#7C3AED] rounded-xl px-3 py-2 font-mono text-xs text-[#F8FAFC] tracking-widest uppercase focus:outline-none"
                    />
                    <button
                      onClick={handleCompletePairing}
                      className="px-3.5 py-2 rounded-xl bg-[#23193B] hover:bg-[#7C3AED] text-[#F8FAFC] text-xs font-semibold transition"
                    >
                      Conectar
                    </button>
                  </div>
                </div>

                {/* Connected TV Status Card */}
                <div className="bg-[#161124] border border-[#23193B] rounded-xl p-3 flex items-center justify-between">
                  <div>
                    <span className="block text-[10px] text-[#94A3B8]">
                      {isPaired ? 'TV Conectada' : 'Estado da Conexão'}
                    </span>
                    <span className="text-xs font-bold text-[#F8FAFC]">
                      {isPaired ? 'Sala de Estar • Sony Bravia XR' : 'Aguardando código...'}
                    </span>
                  </div>
                  {isPaired && (
                    <button
                      onClick={() => setMobileScreen('games')}
                      className="px-3 py-1.5 rounded-lg bg-[#7C3AED] text-[#F8FAFC] text-[11px] font-semibold"
                    >
                      Ir aos Jogos
                    </button>
                  )}
                </div>
              </div>
            )}

            {/* ---------------- MOBILE SCREEN 4: MEUS JOGOS ---------------- */}
            {mobileScreen === 'games' && (
              <div className="relative flex flex-col h-full px-4 py-3.5">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setMobileScreen('home')}
                      className="p-1.5 rounded-lg bg-[#161124] text-[#94A3B8] hover:text-[#F8FAFC]"
                    >
                      <ChevronLeft className="w-4 h-4" />
                    </button>
                    <div>
                      <h3 className="text-sm font-bold text-[#F8FAFC]">Meus Jogos</h3>
                      <span className="text-[10px] text-[#A78BFA]">
                        {isPaired ? 'Conectado à TV • Pronto' : 'Conecte à TV para jogar'}
                      </span>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#161124] text-[#94A3B8] border border-[#23193B]">
                    {installedGames.length} APPS
                  </span>
                </div>

                {/* Vertical Game List */}
                <div className="flex-1 overflow-y-auto space-y-2.5 pr-0.5">
                  {installedGames.map((game) => (
                    <div
                      key={game.id}
                      className="bg-[#161124] border border-[#23193B] hover:border-[#7C3AED]/50 rounded-2xl p-3 transition"
                    >
                      <div className="flex items-center gap-3">
                        <GameIconSvg type={game.icon} />
                        <div className="flex-1 min-w-0">
                          <h4 className="text-xs font-bold text-[#F8FAFC] truncate">
                            {game.title}
                          </h4>
                          <p className="text-[10px] text-[#94A3B8] truncate mt-0.5">
                            {game.genre}
                          </p>
                          <span className="inline-block mt-1 text-[10px] text-[#A78BFA] bg-[#09080D] px-2 py-0.5 rounded border border-[#23193B]">
                            {game.profile}
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 mt-2.5 pt-2 border-t border-[#23193B]/70">
                        <button
                          onClick={() => {
                            setSelectedGame(game);
                            setIsCustomizing(true);
                            setMobileScreen('controller');
                          }}
                          className="flex items-center justify-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-[#09080D] hover:bg-[#23193B] text-[#94A3B8] hover:text-[#F8FAFC] text-[11px] border border-[#23193B] transition"
                          title="Configurar Controle Individual"
                        >
                          <Sliders className="w-3 h-3 text-[#8B5CF6]" />
                          <span>Controle</span>
                        </button>

                        <button
                          onClick={() => handleSelectGameToPlay(game)}
                          className="flex-1 flex items-center justify-center gap-1.5 py-1.5 rounded-lg bg-[#7C3AED] hover:bg-[#8B5CF6] text-[#F8FAFC] text-[11px] font-semibold transition"
                        >
                          <Play className="w-3 h-3 fill-current" />
                          <span>Jogar na TV</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Android Screen & Audio Capture Authorization Modal */}
                {showCaptureModal && (
                  <div className="absolute inset-0 bg-[#000000]/80 backdrop-blur-xs flex items-center justify-center p-4 z-30">
                    <div className="w-full bg-[#161124] border border-[#7C3AED]/60 rounded-2xl p-4 space-y-3 shadow-2xl">
                      <div className="flex items-center gap-2.5">
                        <div className="w-9 h-9 rounded-xl bg-[#23193B] flex items-center justify-center text-[#A78BFA]">
                          <ShieldAlert className="w-5 h-5" />
                        </div>
                        <div>
                          <h4 className="text-xs font-bold text-[#F8FAFC]">
                            Autorizar captura de tela e áudio?
                          </h4>
                          <span className="text-[10px] text-[#94A3B8]">
                            Android MediaProjection • NEXA
                          </span>
                        </div>
                      </div>

                      <p className="text-[11px] text-[#94A3B8] leading-relaxed">
                        O jogo <strong className="text-[#F8FAFC]">{selectedGame?.title}</strong> será transmitido para a TV. A tela do celular exibirá <strong className="text-[#F8FAFC]">apenas os controles virtuais</strong>.
                      </p>

                      <div className="grid grid-cols-2 gap-2 pt-1">
                        <button
                          onClick={() => setShowCaptureModal(false)}
                          className="py-2 rounded-xl bg-[#09080D] text-[#94A3B8] text-xs font-medium border border-[#23193B]"
                        >
                          Cancelar
                        </button>
                        <button
                          onClick={handleAuthorizeAndStart}
                          className="py-2 rounded-xl bg-[#7C3AED] hover:bg-[#8B5CF6] text-[#F8FAFC] text-xs font-semibold"
                        >
                          Iniciar Agora
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* ---------------- MOBILE SCREEN 5: TELA DE CONTROLE (PURE BLACK OLED) ---------------- */}
            {mobileScreen === 'controller' && (
              <div
                ref={controllerPadRef}
                onPointerMove={handlePadPointerMove}
                onPointerUp={handlePadPointerUp}
                onPointerLeave={handlePadPointerUp}
                className="relative w-full h-full bg-[#000000] select-none touch-none overflow-hidden flex flex-col justify-between"
              >
                {/* STRICT RULE: ZERO GAME VIDEO OR WALLPAPER HERE — PURE #000000 OLED */}

                {/* Top Minimal Utility Bar */}
                <div className="relative z-20 flex items-center justify-between px-4 py-2 border-b border-[#161124]">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-[#8B5CF6]" />
                    <span className="text-[11px] font-medium text-[#94A3B8]">
                      TV Conectada • <span className="font-mono text-[#F8FAFC]">5ms</span>
                    </span>
                    <span className="hidden sm:inline text-[10px] text-[#7C3AED] font-semibold px-2 py-0.5 rounded bg-[#161124]">
                      MODO OLED PRETO
                    </span>
                  </div>

                  {/* Controller Utility Actions */}
                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => setIsCustomizing((prev) => !prev)}
                      className={`flex items-center gap-1 px-2.5 py-1 rounded-lg text-[11px] font-medium border transition ${
                        isCustomizing
                          ? 'bg-[#7C3AED] text-[#F8FAFC] border-[#8B5CF6]'
                          : 'bg-[#09080D] text-[#94A3B8] border-[#23193B] hover:text-[#F8FAFC]'
                      }`}
                    >
                      <Sliders className="w-3 h-3" />
                      <span>{isCustomizing ? 'Concluir' : 'Personalizar'}</span>
                    </button>

                    <button
                      onClick={() => setIsPaused((prev) => !prev)}
                      className={`flex items-center gap-1 px-2.5 py-1 rounded-lg text-[11px] font-medium border transition ${
                        isPaused
                          ? 'bg-[#23193B] text-[#F8FAFC] border-[#7C3AED]'
                          : 'bg-[#09080D] text-[#94A3B8] border-[#23193B] hover:text-[#F8FAFC]'
                      }`}
                    >
                      {isPaused ? <Play className="w-3 h-3" /> : <Pause className="w-3 h-3" />}
                      <span>{isPaused ? 'Retomar' : 'Pausar'}</span>
                    </button>

                    <button
                      onClick={handleDisconnect}
                      className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#09080D] hover:bg-[#161124] text-[#94A3B8] hover:text-[#F8FAFC] text-[11px] font-medium border border-[#23193B]"
                    >
                      <Power className="w-3 h-3 text-[#A78BFA]" />
                      <span>Encerrar</span>
                    </button>
                  </div>
                </div>

                {/* Shoulder Triggers (L1 / R1) */}
                <div className="relative z-10 flex items-center justify-between px-6 pt-2">
                  <button
                    onPointerDown={() => triggerVirtualButton('L1')}
                    className={`px-6 py-1.5 rounded-xl font-mono text-xs font-bold border transition ${
                      activeBtn === 'L1'
                        ? 'bg-[#7C3AED] text-[#F8FAFC] border-[#A78BFA] scale-95'
                        : 'bg-[#09080D] text-[#94A3B8] border-[#23193B]'
                    }`}
                  >
                    L1
                  </button>
                  <button
                    onPointerDown={() => triggerVirtualButton('R1')}
                    className={`px-6 py-1.5 rounded-xl font-mono text-xs font-bold border transition ${
                      activeBtn === 'R1'
                        ? 'bg-[#7C3AED] text-[#F8FAFC] border-[#A78BFA] scale-95'
                        : 'bg-[#09080D] text-[#94A3B8] border-[#23193B]'
                    }`}
                  >
                    R1
                  </button>
                </div>

                {/* Customization Floating Toolbar (when active) */}
                {isCustomizing && (
                  <div className="relative z-30 mx-auto my-auto bg-[#161124]/95 border border-[#7C3AED] rounded-xl px-3.5 py-2 flex items-center gap-3 shadow-xl">
                    <span className="text-[10px] text-[#A78BFA] font-semibold flex items-center gap-1">
                      <Move className="w-3 h-3" /> Arraste os botões
                    </span>
                    <div className="flex items-center gap-1.5">
                      <Maximize2 className="w-3 h-3 text-[#94A3B8]" />
                      <input
                        type="range"
                        min="0.8"
                        max="1.35"
                        step="0.05"
                        value={controlsConfig[selectedControlId]?.size || buttonScale}
                        onChange={(e) => updateSelectedScale(parseFloat(e.target.value))}
                        className="w-20 accent-[#7C3AED]"
                      />
                    </div>
                    <button
                      onClick={resetControlsLayout}
                      className="text-[10px] text-[#94A3B8] hover:text-[#F8FAFC] flex items-center gap-1"
                      title="Restaurar Padrão"
                    >
                      <RotateCcw className="w-3 h-3" /> Reset
                    </button>
                  </div>
                )}

                {/* Draggable / Interactive Virtual Analog Joystick */}
                <div
                  style={{
                    left: `${controlsConfig.joystick.x}%`,
                    top: `${controlsConfig.joystick.y}%`,
                    transform: `translate(-50%, -50%) scale(${controlsConfig.joystick.size})`,
                  }}
                  onPointerDown={() => {
                    if (isCustomizing) {
                      setSelectedControlId('joystick');
                      draggingControlRef.current = 'joystick';
                    } else {
                      draggingJoyRef.current = true;
                    }
                  }}
                  className={`absolute w-24 h-24 rounded-full bg-[#09080D] border-2 flex items-center justify-center transition-colors ${
                    isCustomizing && selectedControlId === 'joystick'
                      ? 'border-[#A78BFA] ring-4 ring-[#7C3AED]/30 cursor-move'
                      : 'border-[#23193B]'
                  }`}
                >
                  {/* Inner Analog Thumbstick */}
                  <div
                    style={{
                      transform: `translate(${joystickVec.x * 24}px, ${joystickVec.y * 24}px)`,
                    }}
                    className="w-11 h-11 rounded-full bg-[#161124] border-2 border-[#7C3AED] shadow-[0_0_20px_rgba(124,58,237,0.3)] flex items-center justify-center"
                  >
                    <div className="w-3 h-3 rounded-full bg-[#8B5CF6]" />
                  </div>
                </div>

                {/* Draggable / Interactive Action Buttons (A, B, X, Y) */}
                {['btnA', 'btnB', 'btnX', 'btnY'].map((btnKey) => {
                  const cfg = controlsConfig[btnKey];
                  const isPressed = activeBtn === cfg.label;
                  const isSelected = isCustomizing && selectedControlId === btnKey;

                  return (
                    <button
                      key={btnKey}
                      style={{
                        left: `${cfg.x}%`,
                        top: `${cfg.y}%`,
                        transform: `translate(-50%, -50%) scale(${cfg.size})`,
                      }}
                      onPointerDown={() => {
                        if (isCustomizing) {
                          setSelectedControlId(btnKey);
                          draggingControlRef.current = btnKey;
                        } else {
                          triggerVirtualButton(cfg.label);
                        }
                      }}
                      className={`absolute w-12 h-12 rounded-full font-bold text-sm flex items-center justify-center border-2 transition-colors ${
                        isSelected
                          ? 'bg-[#23193B] border-[#A78BFA] text-[#F8FAFC] ring-4 ring-[#7C3AED]/35 cursor-move'
                          : isPressed
                          ? 'bg-[#7C3AED] border-[#F8FAFC] text-[#F8FAFC] shadow-[0_0_24px_#8B5CF6]'
                          : 'bg-[#09080D] border-[#23193B] text-[#F8FAFC] hover:border-[#7C3AED]'
                      }`}
                    >
                      {cfg.label}
                    </button>
                  );
                })}

                {/* Subtle Bottom OLED Indicator (No game imagery) */}
                <div className="relative z-10 flex items-center justify-center pb-2 text-[10px] text-[#94A3B8]/60 tracking-widest uppercase">
                  {isCustomizing
                    ? `Editando: ${controlsConfig[selectedControlId]?.label} (${controlsConfig[selectedControlId]?.action || 'Movimento'})`
                    : `${selectedGame?.title || 'Aether Drift'} • Vídeo ativo exclusivamente na TV`}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // ==========================================================================
  // RENDER: DESIGN SYSTEM & UX FLOW DOCUMENTATION TAB
  // ==========================================================================
  const renderDesignSystem = () => {
    const swatches = [
      {
        name: 'Pure Black (OLED)',
        hex: '#000000',
        usage: 'Fundo absoluto da tela de controle para economia de bateria e zero distração.',
      },
      {
        name: 'Obsidian Surface',
        hex: '#09080D',
        usage: 'Fundo base das interfaces Android TV e Android Mobile.',
      },
      {
        name: 'Deep Plum Card',
        hex: '#161124',
        usage: 'Superfícies elevadas, cards de jogos, inputs e contêineres.',
      },
      {
        name: 'Royal Violet Accent',
        hex: '#7C3AED',
        usage: 'Ações primárias, anéis de foco D-Pad na TV e botões ativos.',
      },
      {
        name: 'Electric Violet Glow',
        hex: '#8B5CF6',
        usage: 'Estados de hover, indicadores de conexão ativa e telemetria.',
      },
      {
        name: 'Crisp White Text',
        hex: '#F8FAFC',
        usage: 'Tipografia principal de alta legibilidade à distância.',
      },
    ];

    return (
      <div className="max-w-6xl mx-auto space-y-8 py-2">
        {/* Color Palette Section */}
        <section className="bg-[#09080D] border border-[#23193B] rounded-2xl p-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-5">
            <div>
              <h3 className="text-lg font-bold text-[#F8FAFC]">
                Sistema de Cores & Identidade Visual NEXA
              </h3>
              <p className="text-xs text-[#94A3B8]">
                Paleta proprietária em preto profundo, roxo escuro e violeta. Sem ciano ou gradientes genéricos.
              </p>
            </div>
            <span className="text-xs font-mono px-3 py-1 rounded-lg bg-[#161124] text-[#A78BFA] border border-[#7C3AED]/30">
              Contrast Ratio ≥ 12.4:1
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {swatches.map((sw) => (
              <div
                key={sw.hex}
                className="bg-[#161124] border border-[#23193B] rounded-xl p-4 flex items-start gap-3.5"
              >
                <div
                  style={{ backgroundColor: sw.hex }}
                  className="w-12 h-12 rounded-xl border border-[#F8FAFC]/15 shrink-0 shadow-inner"
                />
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-[#F8FAFC]">{sw.name}</span>
                    <span className="font-mono text-[11px] text-[#A78BFA]">{sw.hex}</span>
                  </div>
                  <p className="text-[11px] text-[#94A3B8] mt-1 leading-relaxed">
                    {sw.usage}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Platform Ergonomics Comparison: Android TV vs Android Mobile */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-[#09080D] border border-[#23193B] rounded-2xl p-6 space-y-3">
            <div className="flex items-center gap-2.5 text-[#A78BFA]">
              <Tv className="w-5 h-5" />
              <h4 className="text-base font-bold text-[#F8FAFC]">
                Arquitetura NEXA TV (Android TV / Google TV)
              </h4>
            </div>
            <ul className="space-y-2.5 text-xs text-[#94A3B8]">
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#8B5CF6] shrink-0 mt-0.5" />
                <span>
                  <strong className="text-[#F8FAFC]">Navegação D-Pad:</strong> Todos os elementos interativos possuem anéis de foco violeta de alto contraste visíveis a 3 metros de distância.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#8B5CF6] shrink-0 mt-0.5" />
                <span>
                  <strong className="text-[#F8FAFC]">Pareamento Instantâneo:</strong> QR Code de alta densidade acompanhado de código alfanumérico local (NX-8492).
                </span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#8B5CF6] shrink-0 mt-0.5" />
                <span>
                  <strong className="text-[#F8FAFC]">Transmissão Imersiva:</strong> Vídeo em tela cheia 16:9 com indicador discreto de latência no canto superior.
                </span>
              </li>
            </ul>
          </div>

          <div className="bg-[#09080D] border border-[#23193B] rounded-2xl p-6 space-y-3">
            <div className="flex items-center gap-2.5 text-[#A78BFA]">
              <Smartphone className="w-5 h-5" />
              <h4 className="text-base font-bold text-[#F8FAFC]">
                Arquitetura NEXA Mobile (Android Touch & OLED)
              </h4>
            </div>
            <ul className="space-y-2.5 text-xs text-[#94A3B8]">
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#8B5CF6] shrink-0 mt-0.5" />
                <span>
                  <strong className="text-[#F8FAFC]">Zero Espelhamento Local:</strong> Durante o jogo, o celular desliga pixels com fundo #000000 e exibe exclusivamente os controles.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#8B5CF6] shrink-0 mt-0.5" />
                <span>
                  <strong className="text-[#F8FAFC]">Ergonomia Personalizável:</strong> Ajuste livre de posição (arrastar), escala e mapeamento individual por jogo.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#8B5CF6] shrink-0 mt-0.5" />
                <span>
                  <strong className="text-[#F8FAFC]">Permissão Explícita:</strong> Fluxo nativo de autorização de captura de tela e áudio antes do início da partida.
                </span>
              </li>
            </ul>
          </div>
        </section>
      </div>
    );
  };

  // ==========================================================================
  // MAIN SHELL LAYOUT
  // ==========================================================================
  const currentStep = getCurrentStep();

  return (
    <div className="min-h-screen bg-[#050408] text-[#F8FAFC] flex flex-col font-sans selection:bg-[#7C3AED] selection:text-[#F8FAFC]">
      {/* Top Showcase Header */}
      <header className="sticky top-0 z-40 bg-[#09080D]/95 backdrop-blur-md border-b border-[#23193B] px-4 sm:px-6 py-3">
        <div className="max-w-7xl mx-auto flex flex-col lg:flex-row lg:items-center justify-between gap-3">
          {/* Brand & Subtitle */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3.5">
              <NexaLogo size="sm" />
              <div className="hidden sm:block h-5 w-px bg-[#23193B]" />
              <span className="hidden sm:inline-block text-xs text-[#94A3B8]">
                UI/UX Design System & Protótipo Cross-Device
              </span>
            </div>

            <span className="lg:hidden text-[11px] font-mono px-2.5 py-1 rounded-md bg-[#161124] text-[#A78BFA] border border-[#23193B]">
              Passo {currentStep}/8
            </span>
          </div>

          {/* View Mode Switcher */}
          <nav className="flex flex-wrap items-center gap-1.5 bg-[#161124] p-1 rounded-xl border border-[#23193B]">
            {[
              { id: 'split', label: 'Experiência Integrada', icon: MonitorPlay },
              { id: 'tv', label: 'App 1: NEXA TV', icon: Tv },
              { id: 'mobile', label: 'App 2: NEXA Mobile', icon: Smartphone },
              { id: 'specs', label: 'Design System', icon: Layers },
            ].map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => setViewMode(item.id)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    viewMode === item.id
                      ? 'bg-[#7C3AED] text-[#F8FAFC] shadow-sm'
                      : 'text-[#94A3B8] hover:text-[#F8FAFC]'
                  }`}
                >
                  <Icon className="w-3.5 h-3.5" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>
      </header>

      {/* Interactive 8-Step User Flow Bar */}
      <div className="bg-[#09080D] border-b border-[#161124] px-4 sm:px-6 py-2.5">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-semibold uppercase tracking-[0.18em] text-[#A78BFA]">
              Jornada do Usuário (Clique para testar cada etapa)
            </span>
            <span className="hidden sm:inline text-[11px] text-[#94A3B8]">
              Interaja diretamente nos dispositivos abaixo para sincronizar TV e Celular
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-1.5">
            {uxSteps.map((s) => {
              const isActive = currentStep === s.step;
              return (
                <button
                  key={s.step}
                  onClick={() => {
                    if (viewMode === 'specs') setViewMode('split');
                    s.apply();
                  }}
                  className={`text-left p-2 rounded-xl border transition-all ${
                    isActive
                      ? 'bg-[#161124] border-[#7C3AED] shadow-[0_0_20px_rgba(124,58,237,0.18)]'
                      : 'bg-[#09080D] border-[#23193B]/70 hover:border-[#23193B] opacity-75 hover:opacity-100'
                  }`}
                >
                  <div className="flex items-center gap-1.5">
                    <span
                      className={`w-4 h-4 rounded-full text-[10px] font-bold flex items-center justify-center ${
                        isActive
                          ? 'bg-[#7C3AED] text-[#F8FAFC]'
                          : 'bg-[#161124] text-[#94A3B8]'
                      }`}
                    >
                      {s.step}
                    </span>
                    <span className="text-[11px] font-semibold text-[#F8FAFC] truncate">
                      {s.title}
                    </span>
                  </div>
                  <p className="text-[10px] text-[#94A3B8] truncate mt-1">
                    {s.desc}
                  </p>
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Main Interactive Prototype Stage */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 py-6">
        {viewMode === 'split' && (
          <div className="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
            {/* Left / Primary: Android TV 16:9 Viewport */}
            <div className="xl:col-span-7">{renderNexaTv()}</div>

            {/* Right / Companion: Android Mobile Viewport */}
            <div className="xl:col-span-5 flex justify-center">
              {renderNexaMobile()}
            </div>
          </div>
        )}

        {viewMode === 'tv' && (
          <div className="max-w-4xl mx-auto">{renderNexaTv()}</div>
        )}

        {viewMode === 'mobile' && (
          <div className="max-w-2xl mx-auto flex justify-center">
            {renderNexaMobile()}
          </div>
        )}

        {viewMode === 'specs' && renderDesignSystem()}
      </main>
    </div>
  );
}