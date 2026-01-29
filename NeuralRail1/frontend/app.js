/**
 * NeuralRail - Section Controller Display
 * Main Application Logic
 */

// ============================================
// CONFIGURATION
// ============================================
const CONFIG = {
  API_BASE: 'http://localhost:5000/api',
  UPDATE_INTERVAL: 800, // ms - slower updates for realistic movement (use speed controls to go faster)
  ANIMATION_DURATION: 800, // ms - match update interval for smooth transitions
};

// Route Data - DELHI SECTION (Hub-based layout with 4 directions)
// Using external SVG (delhi_junction.svg) for track schematic
const ROUTE = {
  totalKm: 141,  // Max distance (to Mathura on South line)
  hub: 'NDLS',   // New Delhi is the central hub
  
  // Stations organized by line
  stations: [
    // Hub
    { code: 'NDLS', name: 'New Delhi', km: 0, elevation: 216, line: 'HUB' },
    // North Line
    { code: 'DLI', name: 'Old Delhi Jn', km: 7, elevation: 216, line: 'NORTH' },
    { code: 'NRL', name: 'Narela', km: 32, elevation: 218, line: 'NORTH' },
    { code: 'SNP', name: 'Sonipat', km: 42, elevation: 220, line: 'NORTH' },
    // South Line
    { code: 'NZM', name: 'Nizamuddin', km: 5, elevation: 214, line: 'SOUTH' },
    { code: 'FDB', name: 'Faridabad', km: 25, elevation: 210, line: 'SOUTH' },
    { code: 'PWL', name: 'Palwal', km: 60, elevation: 200, line: 'SOUTH' },
    { code: 'MTJ', name: 'Mathura Jn', km: 141, elevation: 174, line: 'SOUTH' },
    // East Line
    { code: 'ANVT', name: 'Anand Vihar', km: 12, elevation: 212, line: 'EAST' },
    { code: 'GZB', name: 'Ghaziabad', km: 25, elevation: 210, line: 'EAST' },
    // West Line
    { code: 'DSB', name: 'Sadar Bazar', km: 4, elevation: 216, line: 'WEST' },
    { code: 'DEE', name: 'Sarai Rohilla', km: 10, elevation: 218, line: 'WEST' },
    { code: 'DEC', name: 'Delhi Cantt', km: 15, elevation: 220, line: 'WEST' }
  ],
  
  // Lines with their properties
  lines: {
    NORTH: { color: '#00ff88', name: 'North Line (Punjab)', tracks: 2 },
    SOUTH: { color: '#00d4ff', name: 'South Line (Agra)', tracks: 3 },
    EAST: { color: '#ff6b35', name: 'East Line (Lucknow)', tracks: 2 },
    WEST: { color: '#a855f7', name: 'West Line (Jaipur)', tracks: 2 }
  },
  
  // Loops available for conflict resolution
  loops: [
    { name: 'Ghaziabad Overtaking Loop', station: 'GZB', line: 'EAST', type: 'overtaking' },
    { name: 'Sadar Bazar Holding Loop', station: 'DSB', line: 'WEST', type: 'holding' },
    { name: 'Nizamuddin Passing Loop', station: 'NZM', line: 'SOUTH', type: 'passing' }
  ],
  
  // For compatibility with existing code - sections as linear segments
  sections: [
    { from: 0, to: 42, tracks: 2, name: 'NORTH LINE', type: 'mainline', maxSpeed: 110, line: 'NORTH' },
    { from: 0, to: 141, tracks: 3, name: 'SOUTH LINE', type: 'mainline', maxSpeed: 130, line: 'SOUTH' },
    { from: 0, to: 25, tracks: 2, name: 'EAST LINE', type: 'mainline', maxSpeed: 100, line: 'EAST' },
    { from: 0, to: 15, tracks: 2, name: 'WEST LINE', type: 'mainline', maxSpeed: 100, line: 'WEST' }
  ]
};

// Use external SVG for track schematic
const USE_EXTERNAL_SVG = true;
const EXTERNAL_SVG_PATH = 'delhi_junction.svg';

// Train Colors
const TRAIN_COLORS = {
  rajdhani: '#ff4444',
  freight_heavy: '#cd853f',
  vande_bharat: '#ffd700',
  local_emu: '#4a90d9',
  express_passenger: '#32cd32'
};

// ============================================
// STATE
// ============================================
let state = {
  connected: false,
  scenarios: [],
  currentScenario: null,
  trackInfo: null,  // Track info from scenario (blocked tracks, etc.)
  trains: [],
  conflict: null,
  solutions: [],
  isRunning: false,
  zoom: 100,
  energyConsumed: 0,
  energySaved: 0,
  graphData: [],
  // AI Performance metrics
  aiPerformance: {
    conflictsDetected: 0,
    conflictsResolved: 0,
    totalResponseTime: 0,
    responseCount: 0,
    solutionsGenerated: 0,
    priorityRespected: null,  // true/false/null
    lastResponseTime: 0
  },
  // Energy tracking
  energyTracking: {
    sessionSaved: 37,       // kWh saved this session (initial demo value)
    sessionCO2Saved: 30.3,  // kg CO2 saved this session
    withoutAI: 65,          // Energy that would be used without AI
    withAI: 28,             // Energy used with AI
    delhiSectionTotal: 850, // Total saved in Delhi section (simulated)
    divisionTotal: 2450     // Total saved in Northern Division (simulated)
  },
  // Simulation timing
  simulationStartTime: null,
  // Position-based conflict detection: detect when main trains are within this distance
  conflictDetectionDistance: 20,  // Detect conflict when RAJ and FRT are within 20 km
  conflictAlreadyHandled: false,  // Prevent multiple conflict detections
  // Playback controls
  playbackSpeed: 1,  // 1 = normal, 2 = 2x, 0.5 = half speed
  // Solution execution state
  executingSolution: null,  // Currently executing solution
  executionPhase: 0,  // 0=not executing, 1=slowing, 2=stopping, 3=passing, 4=complete
  executionPaused: false,  // Pause during execution for explanation
  isExecuting: false  // True when solution is being executed (blocks main simulation loop)
};

let updateInterval = null;


// ============================================
// INITIALIZATION
// ============================================
document.addEventListener('DOMContentLoaded', () => {
  initClock();
  initZoomControls();
  initEventListeners();
  initTrainDetailsPanel();
  initEnergyPanel();
  renderTrackSVG();
  updateScrollMode();  // Set initial scroll mode
  initGraph();
  checkBackendConnection();
});

function initClock() {
  updateClock();
  setInterval(updateClock, 1000);
}

function updateClock() {
  const now = new Date();
  const timeStr = now.toLocaleTimeString('en-IN', { hour12: false });
  const dateStr = now.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  
  document.querySelector('.clock-time').textContent = timeStr;
  document.querySelector('.clock-date').textContent = dateStr;
}

// Zoom and Pan state
let zoomState = {
  scale: 1,
  panX: 0,
  panY: 0,
  isDragging: false,
  lastX: 0,
  lastY: 0
};

// Current focused section
let currentFocusSection = 'all';

function initZoomControls() {
  // Section selector - applies zoom/focus to specific route
  const sectionSelect = document.getElementById('sectionSelect');
  if (sectionSelect) {
    sectionSelect.addEventListener('change', (e) => {
      currentFocusSection = e.target.value;
      console.log('Section changed to:', currentFocusSection);
      
      // Apply zoom/focus to the SVG
      applySectionFocus(currentFocusSection);
    });
  } else {
    console.error('Section select element not found!');
  }
  
  // Initialize drag-to-scroll for track container
  initTrackScroll();
}

// Map pan and zoom state
// Map state - only scale changes via dropdown, no mouse interaction
let mapPanZoom = {
  scale: 1,
  panX: 0,
  panY: 0
};

// Initialize track container - STATIC map, no mouse interactions
function initTrackScroll() {
  // Map is static - zoom only controlled by section dropdown
  // No mouse drag, no mouse wheel zoom
  console.log('Track container initialized - static map mode');
}

// Apply pan and zoom transform to the SVG wrapper
function applyMapTransform() {
  const wrapper = document.getElementById('delhiSvgWrapper');
  if (!wrapper) return;
  
  wrapper.style.transform = `translate(${mapPanZoom.panX}px, ${mapPanZoom.panY}px) scale(${mapPanZoom.scale})`;
}

// Reset map view to default
function resetMapView() {
  mapPanZoom.scale = 1;
  mapPanZoom.panX = 0;
  mapPanZoom.panY = 0;
  applyMapTransform();
  addLog('info', '🔄 Map view reset');
}

// Update scroll mode - not needed for static map
function updateScrollMode() {
  // Map is static - no scroll mode changes needed
}

// Scroll the track container to center on the focused section
function scrollToFocusedSection() {
  const container = document.getElementById('trackContainer');
  if (!container || currentFocusSection === 'all') return;
  
  const sectionIdx = parseInt(currentFocusSection);
  const section = ROUTE.sections[sectionIdx];
  if (!section) return;
  
  // Calculate the center km of the focused section
  const centerKm = (section.from + section.to) / 2;
  
  // Calculate scroll position (SVG is 2400px wide for 192km)
  const svgWidth = 2400;
  const padding = 80; // Left padding in SVG
  const trackWidth = svgWidth - padding - 60; // Minus right padding
  
  const scrollRatio = centerKm / ROUTE.totalKm;
  const targetScrollX = padding + (scrollRatio * trackWidth) - (container.clientWidth / 2);
  
  container.scrollLeft = Math.max(0, targetScrollX);
}

function initEventListeners() {
  document.getElementById('clearLog').addEventListener('click', clearLog);
  document.getElementById('rejectAll').addEventListener('click', closeConflictModal);
  document.getElementById('cancelSimulation').addEventListener('click', closeSimulationModal);
  document.getElementById('approveSimulation').addEventListener('click', approveSolution);
  
  // Playback controls (in navbar)
  document.getElementById('pauseBtn').addEventListener('click', togglePause);
  document.querySelectorAll('.speed-btn-nav').forEach(btn => {
    btn.addEventListener('click', () => setPlaybackSpeed(parseFloat(btn.dataset.speed)));
  });
  
  // Train dropdown toggle
  const trainsBtn = document.getElementById('trainsDropdownBtn');
  const trainsPanel = document.getElementById('trainsDropdownPanel');
  const closeBtn = document.getElementById('closeTrainsDropdown');
  
  if (trainsBtn && trainsPanel) {
    trainsBtn.addEventListener('click', () => {
      trainsPanel.classList.toggle('open');
    });
  }
  
  if (closeBtn && trainsPanel) {
    closeBtn.addEventListener('click', () => {
      trainsPanel.classList.remove('open');
    });
  }
  
  // Close dropdown when clicking outside
  document.addEventListener('click', (e) => {
    if (trainsPanel && !trainsPanel.contains(e.target) && !trainsBtn.contains(e.target)) {
      trainsPanel.classList.remove('open');
    }
  });
}

// Initialize train details panel (click-based popup with drag support)
function initTrainDetailsPanel() {
  const panel = document.getElementById('trainDetailsPanel');
  const closeBtn = document.getElementById('closeTrainDetails');
  
  if (closeBtn) {
    closeBtn.addEventListener('click', hideTrainDetailsPanel);
  }
  
  // Close panel with Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      hideTrainDetailsPanel();
    }
  });
  
  // Make panel draggable
  if (panel) {
    initDraggablePanel(panel);
  }
}

// Make a panel draggable
function initDraggablePanel(panel) {
  let isDragging = false;
  let startX, startY, initialX, initialY;
  
  const header = panel.querySelector('.train-details-header');
  if (!header) return;
  
  // Add drag cursor to header
  header.style.cursor = 'move';
  
  header.addEventListener('mousedown', (e) => {
    // Don't drag if clicking on close button
    if (e.target.classList.contains('close-train-details')) return;
    
    isDragging = true;
    startX = e.clientX;
    startY = e.clientY;
    
    // Get current position
    const rect = panel.getBoundingClientRect();
    initialX = rect.left;
    initialY = rect.top;
    
    // Remove bottom/right positioning, use top/left instead
    panel.style.bottom = 'auto';
    panel.style.right = 'auto';
    panel.style.top = initialY + 'px';
    panel.style.left = initialX + 'px';
    
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
    
    e.preventDefault();
  });
  
  function onMouseMove(e) {
    if (!isDragging) return;
    
    const deltaX = e.clientX - startX;
    const deltaY = e.clientY - startY;
    
    let newX = initialX + deltaX;
    let newY = initialY + deltaY;
    
    // Keep panel within viewport bounds
    const panelRect = panel.getBoundingClientRect();
    const maxX = window.innerWidth - panelRect.width;
    const maxY = window.innerHeight - panelRect.height;
    
    newX = Math.max(0, Math.min(newX, maxX));
    newY = Math.max(0, Math.min(newY, maxY));
    
    panel.style.left = newX + 'px';
    panel.style.top = newY + 'px';
  }
  
  function onMouseUp() {
    isDragging = false;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
  }
}

// Initialize energy panel with default values
function initEnergyPanel() {
  updateEnergyPanel();
}

// Update energy panel display
function updateEnergyPanel() {
  const et = state.energyTracking;
  
  // Calculate CO2 saved (0.82 kg CO2 per kWh for Indian grid)
  et.sessionCO2Saved = et.sessionSaved * 0.82;
  
  // Calculate cost saved (₹5 per kWh)
  const costSaved = et.sessionSaved * 5;
  
  // Calculate savings percentage
  const savingsPercent = et.withoutAI > 0 ? ((et.withoutAI - et.withAI) / et.withoutAI * 100) : 0;
  
  // ===== RIGHT COLUMN PANELS (Original) =====
  // Session energy saved
  const sessionSavedEl = document.getElementById('sessionEnergySaved');
  if (sessionSavedEl) {
    sessionSavedEl.textContent = `${et.sessionSaved.toFixed(0)} kWh`;
  }
  
  // CO2 saved
  const co2SavedEl = document.getElementById('sessionCO2Saved');
  if (co2SavedEl) {
    co2SavedEl.textContent = `${et.sessionCO2Saved.toFixed(1)} kg`;
  }
  
  // Energy comparison
  const withoutAIEl = document.getElementById('energyWithoutAI');
  const withAIEl = document.getElementById('energyWithAI');
  if (withoutAIEl) withoutAIEl.textContent = `${et.withoutAI.toFixed(0)} kWh`;
  if (withAIEl) withAIEl.textContent = `${et.withAI.toFixed(0)} kWh`;
  
  // Delhi section saved
  const delhiSavedEl = document.getElementById('delhiSectionSaved');
  if (delhiSavedEl) {
    delhiSavedEl.textContent = `${et.delhiSectionTotal.toFixed(0)} kWh`;
  }
  
  // Division total
  const divisionTotalEl = document.getElementById('divisionTotalSaved');
  if (divisionTotalEl) {
    divisionTotalEl.textContent = `${et.divisionTotal.toLocaleString()} kWh`;
  }
  
  // ===== LEFT COLUMN PANELS (New) =====
  // Session energy saved (big display)
  const sessionSavedLeftEl = document.getElementById('sessionEnergySavedLeft');
  if (sessionSavedLeftEl) {
    sessionSavedLeftEl.textContent = et.sessionSaved.toFixed(0);
  }
  
  // CO2 saved
  const co2SavedLeftEl = document.getElementById('sessionCO2SavedLeft');
  if (co2SavedLeftEl) {
    co2SavedLeftEl.textContent = `${et.sessionCO2Saved.toFixed(1)} kg`;
  }
  
  // Cost saved
  const costSavedEl = document.getElementById('sessionCostSaved');
  if (costSavedEl) {
    costSavedEl.textContent = `₹${costSaved.toFixed(0)}`;
  }
  
  // Energy comparison bars
  const withoutAILeftEl = document.getElementById('energyWithoutAILeft');
  const withAILeftEl = document.getElementById('energyWithAILeft');
  if (withoutAILeftEl) withoutAILeftEl.textContent = `${et.withoutAI.toFixed(0)} kWh`;
  if (withAILeftEl) withAILeftEl.textContent = `${et.withAI.toFixed(0)} kWh`;
  
  // Update bar widths
  const barWithoutAI = document.getElementById('barWithoutAI');
  const barWithAI = document.getElementById('barWithAI');
  if (barWithoutAI && barWithAI && et.withoutAI > 0) {
    barWithoutAI.style.width = '100%';
    barWithAI.style.width = `${(et.withAI / et.withoutAI) * 100}%`;
  }
  
  // Savings percentage
  const savingsPercentEl = document.getElementById('savingsPercentage');
  if (savingsPercentEl) {
    savingsPercentEl.textContent = `${savingsPercent.toFixed(0)}%`;
  }
  
  // Division total (left)
  const divisionTotalLeftEl = document.getElementById('divisionTotalSavedLeft');
  if (divisionTotalLeftEl) {
    divisionTotalLeftEl.textContent = `${et.divisionTotal.toLocaleString()} kWh`;
  }
  
  // Division CO2 saved
  const divisionCO2El = document.getElementById('divisionCO2Saved');
  if (divisionCO2El) {
    const divisionCO2 = (et.divisionTotal * 0.82).toFixed(0);
    divisionCO2El.textContent = `≈ ${Number(divisionCO2).toLocaleString()} kg CO₂ reduced`;
  }
  
  // Delhi section saved (left)
  const delhiSavedLeftEl = document.getElementById('delhiSectionSavedLeft');
  if (delhiSavedLeftEl) {
    delhiSavedLeftEl.textContent = `${et.delhiSectionTotal.toFixed(0)} kWh`;
  }
  
  // Update Delhi section progress bar (percentage of division total)
  const delhiPercent = et.divisionTotal > 0 ? (et.delhiSectionTotal / et.divisionTotal * 100) : 35;
  const delhiProgressBar = document.querySelector('.section-controller-item.active .progress-bar');
  const delhiPercentEl = document.querySelector('.section-controller-item.active .section-percent');
  if (delhiProgressBar) {
    delhiProgressBar.style.width = `${delhiPercent}%`;
  }
  if (delhiPercentEl) {
    delhiPercentEl.textContent = `${delhiPercent.toFixed(0)}%`;
  }
}

// Update energy when a solution is approved
function updateEnergyOnSolutionApproved(solution) {
  const et = state.energyTracking;
  
  console.log('updateEnergyOnSolutionApproved called with solution:', solution);
  
  // Get energy values from solution or use realistic defaults based on solution type
  // Typical energy values for train conflict resolution:
  // - Stop freight train: ~35-45 kWh (braking + restart)
  // - Stop passenger train: ~25-35 kWh (lighter, faster restart)
  // - Slow down: ~15-25 kWh (less energy than full stop)
  // - Without AI (emergency stop): ~60-80 kWh (inefficient braking)
  
  let solutionEnergy = solution.energy_kwh;
  let baselineEnergy = 65; // Energy without AI optimization (emergency stop scenario)
  
  // If no energy value from backend, calculate based on solution type
  if (!solutionEnergy || solutionEnergy === 0) {
    const solutionType = solution.type || 'stop';
    const trainAffected = solution.train_affected || '';
    
    if (solutionType === 'stop') {
      // Stopping a train - check if freight or passenger
      if (trainAffected.includes('FRT') || trainAffected.includes('FREIGHT')) {
        solutionEnergy = 42; // Freight is heavier, more energy to stop/restart
      } else {
        solutionEnergy = 28; // Passenger trains are lighter
      }
    } else if (solutionType === 'slow') {
      solutionEnergy = 18; // Slowing is more efficient than stopping
    } else if (solutionType === 'both_slow') {
      solutionEnergy = 22; // Both trains slow slightly
    } else if (solutionType === 'slow_and_stop' || solutionType === 'multi_step') {
      solutionEnergy = 35; // Multi-step is moderately efficient
    } else {
      solutionEnergy = 30; // Default
    }
  }
  
  // Calculate energy saved (baseline - optimized solution)
  const energySaved = Math.max(0, baselineEnergy - solutionEnergy);
  
  console.log(`Energy calculation: baseline=${baselineEnergy} kWh, solution=${solutionEnergy} kWh, saved=${energySaved} kWh`);
  
  // Update session totals
  et.sessionSaved += energySaved;
  et.withoutAI += baselineEnergy;
  et.withAI += solutionEnergy;
  
  // Update Delhi section total
  et.delhiSectionTotal += energySaved;
  
  // Update division total
  et.divisionTotal += energySaved;
  
  console.log('Updated energy tracking:', et);
  
  // Update display
  updateEnergyPanel();
  
  // Log the savings
  const co2Saved = (energySaved * 0.82).toFixed(1);
  const costSaved = (energySaved * 5).toFixed(0);
  addLog('success', `⚡ Energy saved: ${energySaved.toFixed(0)} kWh (${co2Saved} kg CO₂, ₹${costSaved})`);
}

// ============================================
// PLAYBACK CONTROLS
// ============================================
function togglePause() {
  console.log('togglePause called, current isRunning:', state.isRunning, 'currentScenario:', state.currentScenario?.name);
  
  // Check if a scenario is loaded
  if (!state.currentScenario) {
    addLog('warning', '⚠️ Please select and run a scenario first');
    return;
  }
  
  state.isRunning = !state.isRunning;
  const pauseBtn = document.getElementById('pauseBtn');
  const pauseIcon = document.getElementById('pauseIcon');
  
  if (state.isRunning) {
    console.log('Starting simulation...');
    pauseIcon.textContent = '⏸️';
    pauseBtn.classList.remove('paused');
    // Resume simulation - restart the interval
    startSimulation();
    addLog('info', '▶️ Simulation resumed');
  } else {
    pauseIcon.textContent = '▶️';
    pauseBtn.classList.add('paused');
    // Pause simulation - STOP the interval completely
    if (updateInterval) {
      clearInterval(updateInterval);
      updateInterval = null;
    }
    addLog('info', '⏸️ Simulation paused');
  }
}

function setPlaybackSpeed(speed) {
  state.playbackSpeed = speed;
  
  // Update UI (navbar buttons)
  document.querySelectorAll('.speed-btn-nav').forEach(btn => {
    btn.classList.toggle('active', parseFloat(btn.dataset.speed) === speed);
  });
  
  // Adjust CSS transition duration for train markers based on speed
  // At higher speeds, we need faster transitions to keep up
  const transitionDuration = Math.max(0.1, CONFIG.UPDATE_INTERVAL / speed / 1000);
  document.querySelectorAll('.train-marker').forEach(marker => {
    marker.style.transition = `transform ${transitionDuration}s linear`;
  });
  
  // Restart simulation with new speed
  if (state.isRunning) {
    startSimulation();
  }
  
  addLog('info', `⏩ Playback speed: ${speed}x`);
}

// ============================================
// API FUNCTIONS
// ============================================
async function checkBackendConnection() {
  try {
    const res = await fetch(`${CONFIG.API_BASE}/health`);
    if (res.ok) {
      setConnectionStatus(true);
      loadScenarios();
    } else {
      setConnectionStatus(false);
    }
  } catch (error) {
    setConnectionStatus(false);
    addLog('error', 'Backend connection failed. Start: python backend/api/app.py');
  }
}

function setConnectionStatus(connected) {
  state.connected = connected;
  const indicator = document.getElementById('connectionStatus');
  
  if (connected) {
    indicator.className = 'status-indicator live';
    indicator.querySelector('.status-text').textContent = 'LIVE';
    addLog('success', 'Connected to NeuralRail backend');
  } else {
    indicator.className = 'status-indicator error';
    indicator.querySelector('.status-text').textContent = 'OFFLINE';
  }
}

async function loadScenarios() {
  try {
    const res = await fetch(`${CONFIG.API_BASE}/scenarios`);
    const data = await res.json();
    state.scenarios = data.scenarios || [];
    renderScenarioButtons();
  } catch (error) {
    addLog('error', 'Failed to load scenarios');
  }
}

async function loadScenario(id) {
  try {
    addLog('info', `Loading Scenario ${id}...`);
    
    const res = await fetch(`${CONFIG.API_BASE}/scenario/${id}/start`, { method: 'POST' });
    const data = await res.json();
    
    state.currentScenario = data.scenario;
    state.trackInfo = data.track_info || {};
    state.trains = data.trains || [];
    state.conflict = null;
    state.solutions = [];
    state.energyConsumed = 0;
    state.energySaved = 0;
    state.graphData = [];
    state.simulationStartTime = null;
    state.conflictAlreadyHandled = false;  // Reset for new scenario
    state.isExecuting = false;  // Reset execution state for new scenario
    state.executingSolution = null;
    state.executionPhase = 0;
    
    // Reset AI performance for new scenario
    state.aiPerformance = {
      conflictsDetected: 0,
      conflictsResolved: 0,
      totalResponseTime: 0,
      responseCount: 0,
      correctDecisions: 0,
      totalDecisions: 0
    };
    
    // Reset AI panel
    resetAIPanel();
    
    // Reset sustainability panel to waiting state
    resetSustainabilityPanel();
    
    // Auto-focus track schematic on scenario's section
    autoFocusSection(data.scenario, data.track_info);
    
    updateScenarioButtons();
    console.log('Scenario loaded, trains:', state.trains.length, state.trains.map(t => t.id));
    renderTrains();
    updateTrainList();
    updateMetrics();
    clearGraph();
    
    addLog('success', `Scenario ${id} loaded: ${data.scenario.name}`);
    
    // Auto-start simulation when scenario loads
    state.isRunning = true;
    const pauseIcon = document.getElementById('pauseIcon');
    const pauseBtn = document.getElementById('pauseBtn');
    if (pauseIcon) pauseIcon.textContent = '⏸️';
    if (pauseBtn) pauseBtn.classList.remove('paused');
    
    addLog('info', '▶️ Simulation started automatically');
    startSimulation();
    
  } catch (error) {
    console.error('Failed to load scenario:', error);
    addLog('error', `Failed to load scenario: ${error.message}`);
  }
}

// Auto-focus track schematic based on scenario section
function autoFocusSection(scenario, trackInfo) {
  // Determine which section to focus based on scenario route
  const route = trackInfo?.route || scenario?.route || 'all';
  
  // Map route to section focus
  const routeToSection = {
    'WEST': 'west',
    'EAST': 'east',
    'NORTH': 'north',
    'SOUTH': 'south',
    'ALL': 'all',
    'MULTI': 'all'
  };
  
  currentFocusSection = routeToSection[route] || 'all';
  
  // Update the dropdown to match
  const sectionSelect = document.getElementById('sectionSelect');
  if (sectionSelect) {
    sectionSelect.value = currentFocusSection;
  }
  
  // Re-render track with new focus
  renderTrackSVG();
  
  // Update scroll mode
  updateScrollMode();
  
  // Log the scenario section
  if (route && route !== 'all') {
    addLog('info', `📍 Focused on ${route} Line`);
  }
}


function startSimulation() {
  console.log('startSimulation called, isExecuting:', state.isExecuting, 'isRunning:', state.isRunning);
  
  // Don't start if we're in execution mode (solution being applied)
  if (state.isExecuting) {
    console.log('Simulation blocked - execution in progress');
    return;
  }
  
  if (updateInterval) clearInterval(updateInterval);
  
  // Track when simulation started (for conflict detection delay)
  if (!state.simulationStartTime) {
    state.simulationStartTime = Date.now();
  }
  
  // Calculate update interval - keep it consistent for smooth animation
  // At high speeds, we call the API multiple times per interval instead of shortening interval
  const baseInterval = 400; // Fixed 400ms interval for smooth animation
  const stepsPerInterval = Math.max(1, Math.round(state.playbackSpeed)); // 1x=1, 2x=2, 5x=5 steps
  
  // Update transition duration for all train markers
  const transitionDuration = baseInterval / 1000;
  document.querySelectorAll('.train-marker').forEach(marker => {
    marker.style.transition = `transform ${transitionDuration}s linear`;
  });
  
  console.log('Starting simulation interval, baseInterval:', baseInterval, 'stepsPerInterval:', stepsPerInterval);
  
  updateInterval = setInterval(async () => {
    if (!state.isRunning) {
      console.log('Interval tick but isRunning is false');
      return;
    }
    
    try {
      // At higher speeds, call API multiple times per interval for accuracy
      let data;
      for (let i = 0; i < stepsPerInterval; i++) {
        const res = await fetch(`${CONFIG.API_BASE}/simulation/step`, { method: 'POST' });
        data = await res.json();
      }
      
      console.log('Got simulation data, trains:', data.trains?.length, 'first train pos:', data.trains?.[0]?.position);
      
      // Update trains with smooth transition
      updateTrainPositions(data.trains || []);
      
      // Update energy
      state.energyConsumed = data.system_energy_kwh || 0;
      updateMetrics();
      
      // Update graph
      addGraphPoint(data.trains);
      
      // POSITION-BASED conflict detection for Scenario 1 (West Line)
      // Detect conflict when BOTH trains are before DSB (4km) so both AI solutions are valid
      // DSB is at 4km - the holding loop where either train can stop
      if (!state.conflictAlreadyHandled && data.conflicts && data.conflicts.length > 0) {
        // Find the two main trains (RAJ and FRT)
        const rajTrain = data.trains?.find(t => t.id?.includes('RAJ'));
        const frtTrain = data.trains?.find(t => t.id?.includes('FRT'));
        
        // For West Line scenario: DSB is at 4km
        // Freight starts at 0km going forward (toward DSB)
        // Rajdhani starts at 22km going backward (toward NDLS)
        // Conflict should trigger when:
        // - Freight is still before DSB (position < 4km) 
        // - Rajdhani is still beyond DSB (position > 4km)
        // This ensures both solutions are valid (stop either at DSB)
        
        const DSB_KM = 4;  // Sadar Bazar position
        
        if (rajTrain && frtTrain) {
          const rajPos = rajTrain.position;
          const frtPos = frtTrain.position;
          
          // Trigger when: Freight before DSB AND Rajdhani beyond DSB
          // This is the optimal moment - both can reach DSB to stop
          const freightBeforeDSB = frtPos < DSB_KM;
          const rajdhaniAfterDSB = rajPos > DSB_KM;
          
          // Also add a minimum time delay (5 seconds) so trains visibly move first
          const realTimeElapsed = (Date.now() - state.simulationStartTime) / 1000;
          const minTimeElapsed = realTimeElapsed >= 5;
          
          console.log(`Conflict check: FRT@${frtPos.toFixed(1)}km (before DSB: ${freightBeforeDSB}), RAJ@${rajPos.toFixed(1)}km (after DSB: ${rajdhaniAfterDSB}), time: ${realTimeElapsed.toFixed(1)}s`);
          
          if (freightBeforeDSB && rajdhaniAfterDSB && minTimeElapsed) {
            console.log('🚨 Triggering conflict - both trains in valid positions for AI solutions');
            state.conflictAlreadyHandled = true;
            handleConflict(data.conflicts[0]);
          }
        } else {
          // Fallback: use time-based detection for other scenarios
          const realTimeElapsed = (Date.now() - state.simulationStartTime) / 1000;
          const effectiveSimTime = realTimeElapsed * state.playbackSpeed;
          const triggerThreshold = 10; // seconds
          
          if (effectiveSimTime >= triggerThreshold) {
            state.conflictAlreadyHandled = true;
            handleConflict(data.conflicts[0]);
          }
        }
      }
    } catch (error) {
      console.error('Simulation step failed:', error);
    }
  }, baseInterval);
}

function stopSimulation() {
  state.isRunning = false;
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
}

async function handleConflict(conflict) {
  const startTime = Date.now();
  
  state.conflict = conflict;
  state.isRunning = false; // Pause for decision
  
  // Track conflict detected
  state.aiPerformance.conflictsDetected++;
  
  addLog('danger', `⚠️ CONFLICT DETECTED at ${conflict.position_km?.toFixed(1)} km!`);
  
  // Conflict zone rendering disabled - will be enabled when needed
  // renderConflictZone(conflict);
  
  // Get AI recommendations
  try {
    const res = await fetch(`${CONFIG.API_BASE}/conflict/analyze`, { method: 'POST' });
    const data = await res.json();
    
    state.solutions = data.solutions || [];
    state.energySaved = data.energy_saved_kwh || 0;
    
    // Track AI performance metrics
    const responseTime = (Date.now() - startTime) / 1000;
    state.aiPerformance.totalResponseTime += responseTime;
    state.aiPerformance.responseCount++;
    state.aiPerformance.lastResponseTime = responseTime;
    state.aiPerformance.solutionsGenerated = state.solutions.length;
    
    // Check if AI respected priority
    if (state.solutions.length > 0) {
      const firstSolution = state.solutions[0];
      // Priority is respected if no priority_violation flag
      state.aiPerformance.priorityRespected = !firstSolution.priority_violation;
    }
    
    updateMetrics();
    updateAIPerformance();
    showConflictModal(conflict, state.solutions);
    
    addLog('info', `🤖 AI generated ${state.solutions.length} solutions in ${responseTime.toFixed(2)}s`);
  } catch (error) {
    addLog('error', 'Failed to analyze conflict');
  }
}

// ============================================
// TRACK RENDERING - DELHI HUB SVG
// ============================================
// For Delhi Hub, we load the external SVG (delhi_junction.svg) which shows
// the 4-way hub layout with all stations and loops

function renderTrackSVG() {
  const container = document.getElementById('trackContainer');
  
  if (USE_EXTERNAL_SVG) {
    // Load external Delhi Junction SVG
    loadDelhiSVG(container);
    return;
  }
  
  // Fallback to generated SVG (not used for Delhi)
  renderGeneratedTrackSVG();
}

// Load the Delhi Junction SVG
async function loadDelhiSVG(container) {
  try {
    const response = await fetch(EXTERNAL_SVG_PATH);
    const svgText = await response.text();
    
    // Create a wrapper div for the SVG
    container.innerHTML = `
      <div class="delhi-svg-wrapper" id="delhiSvgWrapper">
        ${svgText}
      </div>
    `;
    
    // Get the SVG element and add train overlay group
    const svgElement = container.querySelector('svg');
    if (svgElement) {
      // Store original viewBox for reset
      const originalViewBox = svgElement.getAttribute('viewBox');
      svgElement.dataset.originalViewBox = originalViewBox;
      
      // Ensure SVG fits properly in container - preserve aspect ratio
      svgElement.setAttribute('preserveAspectRatio', 'xMidYMid meet');
      
      // Add a group for train markers
      const trainGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      trainGroup.id = 'trainMarkers';
      svgElement.appendChild(trainGroup);
      
      // Store reference for train rendering
      window.delhiSvg = svgElement;
      window.delhiSvgLoaded = true;
      
      // Apply initial section focus (show all routes by default)
      applySectionFocus('all');
      
      // If trains are already loaded, render them now
      if (state.trains && state.trains.length > 0) {
        console.log('SVG loaded, rendering trains:', state.trains.length);
        renderTrainsOnDelhiSVG();
      }
    }
    
    console.log('Delhi Junction SVG loaded successfully');
  } catch (error) {
    console.error('Failed to load Delhi SVG:', error);
    container.innerHTML = `
      <div class="svg-error">
        <p>⚠️ Could not load track schematic</p>
        <p>Make sure delhi_junction.svg is in the frontend folder</p>
      </div>
    `;
  }
}

// Section focus configurations - uses SVG viewBox for static zoom
// Map is STATIC - only changes when user selects a section from dropdown
// Format: viewBox string "minX minY width height"
// SVG original size: 1710 x 900
const SECTION_VIEWBOX_CONFIG = {
  'all': '0 0 1710 900',           // Full view - shows complete map
  'north': '650 50 450 450',       // North line (NDLS to Sonipat)
  'south': '650 400 450 500',      // South line (NDLS to Mathura) - includes Mathura
  'east': '800 280 900 380',       // East line (NDLS to Ghaziabad)
  'west': '50 280 900 380'         // West line (NDLS to Delhi Cantt)
};

// Apply section focus using SVG viewBox (STATIC - no mouse interaction)
function applySectionFocus(section) {
  const svgElement = window.delhiSvg;
  if (!svgElement) return;
  
  const viewBox = SECTION_VIEWBOX_CONFIG[section] || SECTION_VIEWBOX_CONFIG['all'];
  
  // Apply the viewBox to zoom/focus on the section
  svgElement.setAttribute('viewBox', viewBox);
  
  const sectionNames = {
    'all': 'All Routes (Overview)',
    'north': 'North Line (NDLS → Sonipat)',
    'south': 'South Line (NDLS → Mathura)',
    'east': 'East Line (NDLS → Ghaziabad)',
    'west': 'West Line (NDLS → Delhi Cantt)'
  };
  addLog('info', `🔍 View: ${sectionNames[section]}`);
  
  // Re-render trains to update their positions
  if (state.trains && state.trains.length > 0) {
    renderTrainsOnDelhiSVG();
  }
}

// Apply map transform - simplified for static map
function applyMapTransform() {
  // Map is static - no transform needed
}

// Station positions on the Delhi SVG (x, y coordinates)
// These map station codes to their visual positions on the SVG
// ALIGNED WITH ACTUAL TRACK LINES IN SVG
const DELHI_SVG_POSITIONS = {
  // Hub - center of New Delhi
  'NDLS': { x: 900, y: 450, line: 'HUB' },
  
  // North Line (going up from hub) - tracks at x=885 (T1) and x=915 (T2)
  'DLI': { x: 875, y: 380, line: 'NORTH' },
  'NRL': { x: 900, y: 255, line: 'NORTH' },
  'SNP': { x: 900, y: 175, line: 'NORTH' },
  
  // South Line (going down from hub) - tracks at x=885, 900, 915
  'NZM': { x: 900, y: 535, line: 'SOUTH' },
  'FDB': { x: 900, y: 615, line: 'SOUTH' },
  'PWL': { x: 900, y: 695, line: 'SOUTH' },
  'MTJ': { x: 900, y: 775, line: 'SOUTH' },
  
  // East Line (going right from hub) - tracks at y=435 (T1) and y=465 (T2)
  'ANVT': { x: 1240, y: 450, line: 'EAST' },
  'GZB': { x: 1490, y: 450, line: 'EAST' },
  
  // West Line (going left from hub) - tracks at y=435 (T1) and y=465 (T2)
  // Stations aligned with track center (y=450)
  'DSB': { x: 640, y: 450, line: 'WEST' },
  'DEE': { x: 430, y: 450, line: 'WEST' },
  'DEC': { x: 230, y: 450, line: 'WEST' }
};

// Track Y positions for each line (for proper train alignment)
const TRACK_Y_POSITIONS = {
  'WEST': { T1: 435, T2: 465, center: 450 },
  'EAST': { T1: 435, T2: 465, center: 450 },
  'NORTH': { T1: 885, T2: 915, center: 900 },  // These are X positions (vertical tracks)
  'SOUTH': { T1: 885, T2: 900, T3: 915, center: 900 }  // These are X positions (vertical tracks)
};

// Get position for a train based on its route and km position
function getTrainSVGPosition(train) {
  const route = train.route || detectRouteFromPosition(train);
  const position = train.position;
  const track = train.track || 1;  // Default to track 1
  
  // Get base position from interpolation
  let pos;
  switch(route) {
    case 'NORTH':
      pos = interpolateNorthLine(position);
      break;
    case 'SOUTH':
      pos = interpolateSouthLine(position);
      break;
    case 'EAST':
      pos = interpolateEastLine(position, track);
      break;
    case 'WEST':
      pos = interpolateWestLine(position, track);
      break;
    default:
      pos = { ...DELHI_SVG_POSITIONS['NDLS'] };
  }
  
  return pos;
}

// Interpolate position on North Line (NDLS -> DLI -> NRL -> SNP)
// Track 1 (T1) is at x=885, Track 2 (T2) is at x=915 (vertical tracks)
function interpolateNorthLine(km, track = 1) {
  const trackX = track === 1 ? 885 : 915;
  const stations = [
    { code: 'NDLS', km: 0, x: trackX, y: 450 },
    { code: 'DLI', km: 7, x: trackX - (track === 1 ? 10 : -10), y: 380 },
    { code: 'NRL', km: 32, x: trackX, y: 255 },
    { code: 'SNP', km: 42, x: trackX, y: 175 },
    { code: 'BEYOND', km: 55, x: trackX, y: 100 }
  ];
  return interpolateBetweenStations(stations, km);
}

// Interpolate position on South Line (NDLS -> NZM -> FDB -> PWL -> MTJ)
// Track 1 (T1) is at x=885, Track 2 (T2) is at x=900, Track 3 (T3) is at x=915
function interpolateSouthLine(km, track = 1) {
  const trackX = track === 1 ? 885 : (track === 2 ? 900 : 915);
  const stations = [
    { code: 'NDLS', km: 0, x: trackX, y: 470 },
    { code: 'NZM', km: 5, x: trackX, y: 535 },
    { code: 'FDB', km: 25, x: trackX, y: 615 },
    { code: 'PWL', km: 60, x: trackX, y: 695 },
    { code: 'MTJ', km: 141, x: trackX, y: 775 },
    { code: 'BEYOND', km: 160, x: trackX, y: 850 }
  ];
  return interpolateBetweenStations(stations, km);
}

// Interpolate position on East Line (NDLS -> ANVT -> GZB)
// Track 1 (T1) is at y=435, Track 2 (T2) is at y=465
function interpolateEastLine(km, track = 1) {
  const trackY = track === 1 ? 435 : 465;
  const stations = [
    { code: 'NDLS', km: 0, x: 920, y: trackY },
    { code: 'ANVT', km: 12, x: 1240, y: trackY },
    { code: 'GZB', km: 25, x: 1490, y: trackY },
    { code: 'BEYOND', km: 35, x: 1660, y: trackY }
  ];
  return interpolateBetweenStations(stations, km);
}

// Interpolate position on West Line (NDLS -> DSB -> DEE -> DEC -> Beyond)
// TRACK LAYOUT:
// - NDLS to DSB (0-4 km): SINGLE TRACK at y=450
// - DSB onwards (4-25 km): DOUBLE TRACK at y=435 (T1) and y=465 (T2)
function interpolateWestLine(km, track = 1) {
  // Single track section (0-4 km): y=450
  // Double track section (4+ km): T1=435, T2=465
  
  if (km <= 4) {
    // Single track section - all trains on center line y=450
    const stations = [
      { code: 'NDLS', km: 0, x: 880, y: 450 },
      { code: 'DSB', km: 4, x: 650, y: 450 }  // Junction point
    ];
    return interpolateBetweenStations(stations, km);
  } else {
    // Double track section - trains on T1 (y=435) or T2 (y=465)
    const trackY = track === 1 ? 435 : 465;
    const stations = [
      { code: 'DSB', km: 4, x: 640, y: trackY },
      { code: 'DEE', km: 10, x: 430, y: trackY },
      { code: 'DEC', km: 15, x: 230, y: trackY },
      { code: 'BEYOND', km: 25, x: 50, y: trackY, line: 'WEST' }
    ];
    return interpolateBetweenStations(stations, km);
  }
}

// Generic interpolation between stations
function interpolateBetweenStations(stations, km) {
  // Find the two stations we're between
  for (let i = 0; i < stations.length - 1; i++) {
    const s1 = stations[i];
    const s2 = stations[i + 1];
    
    if (km >= s1.km && km <= s2.km) {
      // Linear interpolation
      const ratio = (km - s1.km) / (s2.km - s1.km);
      return {
        x: s1.x + (s2.x - s1.x) * ratio,
        y: s1.y + (s2.y - s1.y) * ratio,
        line: s1.line
      };
    }
  }
  
  // If beyond range, return last station
  if (km > stations[stations.length - 1].km) {
    return stations[stations.length - 1];
  }
  return stations[0];
}

// Detect route from train's initial position or destination
function detectRouteFromPosition(train) {
  // Use train's route property if available
  if (train.route) return train.route;
  
  // Otherwise, try to detect from train ID or type
  const id = train.id || '';
  if (id.includes('_N')) return 'NORTH';
  if (id.includes('_S')) return 'SOUTH';
  if (id.includes('_E')) return 'EAST';
  if (id.includes('_W')) return 'WEST';
  
  return 'SOUTH'; // Default
}

// Fallback: Generate track SVG (kept for compatibility)
function renderGeneratedTrackSVG() {
  const svg = document.getElementById('trackSvg');
  const container = document.getElementById('trackContainer');
  
  // Simple placeholder for non-Delhi routes
  const width = 1800;
  const height = 350;
  
  svg.setAttribute('viewBox', `0 0 ${width} ${height}`);
  svg.style.width = '100%';
  
  svg.innerHTML = `
    <rect x="0" y="0" width="${width}" height="${height}" fill="#050a12"/>
    <text x="${width/2}" y="${height/2}" text-anchor="middle" fill="#00d4ff" font-size="20">
      Track Schematic - Use Delhi SVG for hub view
    </text>
  `;
}

// Legacy function kept for compatibility - now unused for Delhi
function renderTrackSVGLegacy() {
  const svg = document.getElementById('trackSvg');
  const container = document.getElementById('trackContainer');
  
  const viewStartKm = 0;
  const viewEndKm = ROUTE.totalKm;
  const isFocused = currentFocusSection !== 'all';
  const width = isFocused ? 2400 : 1800;
  const height = 350;
  const padding = { left: 80, right: 60, top: 55, bottom: 70 };
  const baseTrackSpacing = 30;
  const mergeZoneWidth = 60;
  
  svg.setAttribute('viewBox', `0 0 ${width} ${height}`);
  if (isFocused) {
    svg.style.width = `${width}px`;
    svg.style.minWidth = `${width}px`;
  } else {
    svg.style.width = '100%';
    svg.style.minWidth = '100%';
  }
  
  const viewRangeKm = viewEndKm - viewStartKm;
  const trackAreaWidth = width - padding.left - padding.right;
  const trackAreaHeight = height - padding.top - padding.bottom;
  
  svg.innerHTML = '';
  
  const kmToX = (km) => padding.left + ((km - viewStartKm) / viewRangeKm) * trackAreaWidth;
  const centerY = padding.top + trackAreaHeight / 2;
  
  const getTrackYPositions = (numTracks) => {
    const positions = [];
    const totalHeight = (numTracks - 1) * baseTrackSpacing;
    const startY = centerY - totalHeight / 2;
    for (let i = 0; i < numTracks; i++) {
      positions.push(startY + i * baseTrackSpacing);
    }
    return positions;
  };
  
  svg.innerHTML += `<rect x="0" y="0" width="${width}" height="${height}" fill="#050a12"/>`;
  
  const markerInterval = viewRangeKm > 100 ? 20 : viewRangeKm > 50 ? 10 : 5;
  const startMarker = Math.ceil(viewStartKm / markerInterval) * markerInterval;
  for (let km = startMarker; km <= viewEndKm; km += markerInterval) {
    const x = kmToX(km);
    if (x < padding.left || x > width - padding.right) continue;
    const isMajor = km % (markerInterval * 2) === 0;
    svg.innerHTML += `
      <line x1="${x}" y1="${padding.top - 5}" x2="${x}" y2="${height - padding.bottom + 5}" 
            stroke="${isMajor ? 'rgba(255,255,255,0.12)' : 'rgba(255,255,255,0.05)'}" stroke-width="1"/>
      <text x="${x}" y="${height - padding.bottom + 20}" text-anchor="middle" class="distance-label">${km}</text>
    `;
  }
  
  const visibleSections = ROUTE.sections.filter(s => s.to > viewStartKm && s.from < viewEndKm);
  
  visibleSections.forEach((section) => {
    const sIdx = ROUTE.sections.indexOf(section);
    const x1 = Math.max(padding.left, kmToX(section.from));
    const x2 = Math.min(width - padding.right, kmToX(section.to));
    const midX = (x1 + x2) / 2;
    const nextSection = ROUTE.sections[sIdx + 1];
    const prevSection = ROUTE.sections[sIdx - 1];
    
    const isFocusedSection = currentFocusSection !== 'all' && parseInt(currentFocusSection) === sIdx;
    
    const trackColor = section.line ? ROUTE.lines[section.line]?.color || '#00d4ff' : '#00d4ff';
    const trackYs = getTrackYPositions(section.tracks);
    
    if (isFocusedSection) {
      svg.innerHTML += `
        <rect x="${x1 - 5}" y="${padding.top - 10}" width="${x2 - x1 + 10}" height="${trackAreaHeight + 20}" 
              fill="rgba(0,212,255,0.08)" stroke="rgba(0,212,255,0.3)" stroke-width="2" stroke-dasharray="5,3" rx="8"/>
      `;
    }
    
    svg.innerHTML += `
      <text x="${midX}" y="${padding.top - 20}" text-anchor="middle" class="section-label">${section.name}</text>
      <text x="${midX}" y="${padding.top - 5}" text-anchor="middle" class="section-tracks-label">${section.tracks} track${section.tracks > 1 ? 's' : ''}</text>
    `;
    
    let mainStartX = x1;
    let mainEndX = x2;
    
    // If there's a transition at the START (coming from previous section)
    if (prevSection && prevSection.tracks !== section.tracks) {
      mainStartX = x1 + halfMergeZone;
    }
    // If there's a transition at the END (going to next section)
    if (nextSection && nextSection.tracks !== section.tracks) {
      mainEndX = x2 - halfMergeZone;
    }
    
    // Draw main horizontal tracks
    trackYs.forEach((y, tIdx) => {
      svg.innerHTML += `
        <line x1="${mainStartX}" y1="${y}" x2="${mainEndX}" y2="${y}" 
              stroke="${trackColor}" stroke-width="3" stroke-linecap="round"
              class="track-line tracks-${section.tracks}" data-section="${sIdx}" data-track="${tIdx + 1}"/>
      `;
      
      // Track number label (first section only)
      if (sIdx === 0) {
        svg.innerHTML += `
          <text x="${x1 - 8}" y="${y + 4}" text-anchor="end" class="track-num-label">T${tIdx + 1}</text>
        `;
      }
    });
    
    // Draw transitions AT THE JUNCTION using STRAIGHT DIAGONAL LINES
    if (nextSection && nextSection.tracks !== section.tracks) {
      const nextTrackYs = getTrackYPositions(nextSection.tracks);
      const nextStartX = x2 + halfMergeZone; // Next section starts after junction
      
      if (section.tracks > nextSection.tracks) {
        // MERGING at junction: 4→2 or 3→2 (straight diagonal lines)
        // Special handling for 3→2: middle track splits to both outputs
        if (section.tracks === 3 && nextSection.tracks === 2) {
          // Track 0 (top) → Track 0 (top)
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[0]}" x2="${nextStartX}" y2="${nextTrackYs[0]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          // Track 1 (middle) → Track 0 (top) - fork
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[1]}" x2="${nextStartX}" y2="${nextTrackYs[0]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          // Track 1 (middle) → Track 1 (bottom) - fork
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[1]}" x2="${nextStartX}" y2="${nextTrackYs[1]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          // Track 2 (bottom) → Track 1 (bottom)
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[2]}" x2="${nextStartX}" y2="${nextTrackYs[1]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
        } else if (section.tracks === 4 && nextSection.tracks === 2) {
          // 4→2: Top 2 tracks merge to top, bottom 2 merge to bottom
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[0]}" x2="${nextStartX}" y2="${nextTrackYs[0]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[1]}" x2="${nextStartX}" y2="${nextTrackYs[0]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[2]}" x2="${nextStartX}" y2="${nextTrackYs[1]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[3]}" x2="${nextStartX}" y2="${nextTrackYs[1]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
        } else {
          // Generic merge
          trackYs.forEach((y, tIdx) => {
            const targetIdx = Math.floor(tIdx * nextSection.tracks / section.tracks);
            const nextY = nextTrackYs[targetIdx];
            svg.innerHTML += `
              <line x1="${mainEndX}" y1="${y}" x2="${nextStartX}" y2="${nextY}" 
                    stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
            `;
          });
        }
      } else {
        // DIVERGING at junction: 2→3 (straight diagonal lines)
        // Special handling for 2→3: each track splits appropriately
        if (section.tracks === 2 && nextSection.tracks === 3) {
          // Track 0 (top) → Track 0 (top)
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[0]}" x2="${nextStartX}" y2="${nextTrackYs[0]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          // Track 0 (top) → Track 1 (middle) - fork
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[0]}" x2="${nextStartX}" y2="${nextTrackYs[1]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          // Track 1 (bottom) → Track 1 (middle) - fork
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[1]}" x2="${nextStartX}" y2="${nextTrackYs[1]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
          // Track 1 (bottom) → Track 2 (bottom)
          svg.innerHTML += `
            <line x1="${mainEndX}" y1="${trackYs[1]}" x2="${nextStartX}" y2="${nextTrackYs[2]}" 
                  stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
          `;
        } else {
          // Generic diverge
          trackYs.forEach((y, tIdx) => {
            const ratio = nextSection.tracks / section.tracks;
            const startNextIdx = Math.floor(tIdx * ratio);
            const endNextIdx = Math.ceil((tIdx + 1) * ratio) - 1;
            
            for (let nIdx = startNextIdx; nIdx <= endNextIdx; nIdx++) {
              const nextY = nextTrackYs[nIdx];
              svg.innerHTML += `
                <line x1="${mainEndX}" y1="${y}" x2="${nextStartX}" y2="${nextY}" 
                      stroke="${trackColor}" stroke-width="3" stroke-linecap="round" class="track-merge"/>
              `;
            }
          });
        }
      }
    }
  });
  
  // Filter and draw visible stations
  const visibleStations = ROUTE.stations.filter(st => st.km >= viewStartKm - 2 && st.km <= viewEndKm + 2);
  visibleStations.forEach((station) => {
    const x = kmToX(station.km);
    if (x < padding.left - 10 || x > width - padding.right + 10) return;
    
    // Station line (vertical, subtle)
    svg.innerHTML += `
      <line x1="${x}" y1="${padding.top}" x2="${x}" y2="${height - padding.bottom}" 
            stroke="rgba(255,255,255,0.15)" stroke-width="1" stroke-dasharray="3,3"/>
    `;
    
    // Station marker and label
    svg.innerHTML += `
      <circle cx="${x}" cy="${height - padding.bottom + 8}" r="6" class="station-marker"/>
      <text x="${x}" y="${height - padding.bottom + 26}" text-anchor="middle" class="station-label">${station.code}</text>
      <text x="${x}" y="${height - padding.bottom + 40}" text-anchor="middle" class="station-km">${station.km} km</text>
    `;
  });
  
  // Render blocked tracks / incidents from scenario track_info
  renderBlockedTracks(svg, kmToX, getTrackYPositions, viewStartKm, viewEndKm, padding);
  
  // Container for conflict zone and trains
  svg.innerHTML += '<g id="conflictGroup"></g>';
  svg.innerHTML += '<g id="trainsGroup"></g>';
}

// Render blocked tracks and incidents based on scenario track_info
function renderBlockedTracks(svg, kmToX, getTrackYPositions, viewStartKm, viewEndKm, padding) {
  if (!state.trackInfo) return;
  
  const trackInfo = state.trackInfo;
  const sectionStartKm = trackInfo.section_start_km;
  const sectionEndKm = trackInfo.section_end_km;
  const numTracks = trackInfo.tracks || 2;
  
  // Check if this section is in view
  if (sectionEndKm < viewStartKm || sectionStartKm > viewEndKm) return;
  
  const trackYs = getTrackYPositions(numTracks);
  
  // Check each track status
  for (let t = 1; t <= numTracks; t++) {
    const statusKey = `track_${t}_status`;
    const detailsKey = `track_${t}_details`;
    const incidentKey = `track_${t}_incident`;
    
    const status = trackInfo[statusKey];
    const details = trackInfo[detailsKey];
    const incident = trackInfo[incidentKey];
    
    if (status === 'BLOCKED') {
      // Calculate center of the section width
      const sectionCenterKm = (sectionStartKm + sectionEndKm) / 2;
      const x = kmToX(sectionCenterKm);
      const y = trackYs[t - 1];
      
      // Draw blocked indicator - UNDER the track, centered horizontally
      svg.innerHTML += `
        <!-- Blocked Track Label - under track, centered -->
        <text x="${x}" y="${y + 18}" text-anchor="middle" fill="#ff3b3b" font-size="11" font-weight="700" 
              font-family="'JetBrains Mono', monospace">
          T${t} BLOCKED
        </text>
      `;
    }
  }
  
  // Render sidings if available
  if (trackInfo.sidings && trackInfo.sidings.length > 0) {
    trackInfo.sidings.forEach((siding, idx) => {
      const x = kmToX(siding.location_km);
      const trackY = trackYs[0]; // Connect to Track 1
      const sidingLength = 40; // Visual length of siding
      const sidingY = trackY - 25 - (idx * 20); // Above the track
      
      // Draw siding branch line (diagonal connection)
      svg.innerHTML += `
        <line x1="${x}" y1="${trackY}" x2="${x - 15}" y2="${sidingY}" 
              stroke="#ffb800" stroke-width="2" stroke-linecap="round"/>
      `;
      
      // Draw siding track (horizontal dead-end)
      svg.innerHTML += `
        <line x1="${x - 15}" y1="${sidingY}" x2="${x - 15 - sidingLength}" y2="${sidingY}" 
              stroke="#ffb800" stroke-width="3" stroke-linecap="round"/>
      `;
      
      // Draw dead-end marker (buffer stop)
      svg.innerHTML += `
        <rect x="${x - 15 - sidingLength - 4}" y="${sidingY - 5}" width="4" height="10" 
              fill="#ffb800" rx="1"/>
      `;
      
      // Siding label
      svg.innerHTML += `
        <text x="${x - 15 - sidingLength/2}" y="${sidingY - 8}" text-anchor="middle" 
              fill="#ffb800" font-size="9" font-weight="600" font-family="'JetBrains Mono', monospace">
          ${siding.name.split('(')[0].trim()}
        </text>
        <text x="${x - 15 - sidingLength/2}" y="${sidingY + 12}" text-anchor="middle" 
              fill="#8aa4c0" font-size="8" font-family="'JetBrains Mono', monospace">
          ${siding.location_km} km
        </text>
      `;
    });
  }
}

function renderTrains() {
  console.log('renderTrains called, trains count:', state.trains.length, 'USE_EXTERNAL_SVG:', USE_EXTERNAL_SVG);
  
  // For Delhi SVG, render trains on the SVG overlay
  if (USE_EXTERNAL_SVG) {
    renderTrainsOnDelhiSVG();
    return;
  }
  
  // Legacy rendering for linear routes
  const group = document.getElementById('trainsGroup');
  if (!group) return;
  
  group.innerHTML = '';
  
  const transitionDuration = Math.max(0.1, CONFIG.UPDATE_INTERVAL / state.playbackSpeed / 1000);
  
  state.trains.forEach(train => {
    const pos = getTrainPosition(train);
    const color = train.color || TRAIN_COLORS[train.type] || '#00d4ff';
    const isForward = train.direction === 'forward';
    
    const arrowSize = 8;
    const arrowX = isForward ? 18 : -18;
    const arrowPoints = isForward 
      ? `${arrowX - arrowSize},-${arrowSize/2} ${arrowX},0 ${arrowX - arrowSize},${arrowSize/2}`
      : `${arrowX + arrowSize},-${arrowSize/2} ${arrowX},0 ${arrowX + arrowSize},${arrowSize/2}`;
    
    group.innerHTML += `
      <g class="train-marker" id="train-${train.id}" transform="translate(${pos.x}, ${pos.y})" style="transition: transform ${transitionDuration}s linear">
        <circle r="22" fill="${color}" opacity="0.25" class="train-glow"/>
        <circle r="12" class="train-dot" fill="${color}"/>
        <polygon points="${arrowPoints}" class="train-direction" fill="${color}"/>
        <text y="-22" text-anchor="middle" class="train-label">${train.id}</text>
      </g>
    `;
  });
}

// Render trains on the Delhi Junction SVG
function renderTrainsOnDelhiSVG() {
  const svg = window.delhiSvg || document.querySelector('#trackContainer svg');
  if (!svg) {
    console.log('SVG not loaded yet, will retry... trains:', state.trains.length);
    // Retry after a short delay if SVG not loaded
    setTimeout(() => renderTrainsOnDelhiSVG(), 100);
    return;
  }
  
  console.log('Rendering', state.trains.length, 'trains on Delhi SVG, svg element:', svg.tagName);
  
  // Get or create train markers group
  let trainGroup = svg.querySelector('#trainMarkers');
  if (!trainGroup) {
    trainGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    trainGroup.id = 'trainMarkers';
    svg.appendChild(trainGroup);
  }
  
  trainGroup.innerHTML = '';
  
  const transitionDuration = Math.max(0.1, CONFIG.UPDATE_INTERVAL / state.playbackSpeed / 1000);
  
  state.trains.forEach(train => {
    const pos = getTrainPositionDelhi(train);
    const color = train.color || TRAIN_COLORS[train.type] || '#00d4ff';
    const route = train.route || detectRouteFromPosition(train);
    
    // Calculate rotation angle based on route direction
    // North/South = vertical (0°), East/West = horizontal (90°)
    let rotation = 0;
    if (route === 'EAST' || route === 'WEST') {
      rotation = 0; // Horizontal for East/West lines
    } else {
      rotation = 90; // Vertical for North/South lines
    }
    
    // Create train marker group
    const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    g.setAttribute('class', 'train-marker');
    g.setAttribute('id', `train-${train.id}`);
    g.setAttribute('transform', `translate(${pos.x}, ${pos.y})`);
    g.style.transition = `transform ${transitionDuration}s linear`;
    
    // Glow effect (rectangular)
    const glow = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    glow.setAttribute('x', '-18');
    glow.setAttribute('y', '-8');
    glow.setAttribute('width', '36');
    glow.setAttribute('height', '16');
    glow.setAttribute('rx', '3');
    glow.setAttribute('fill', color);
    glow.setAttribute('opacity', '0.3');
    glow.setAttribute('class', 'train-glow');
    glow.setAttribute('transform', `rotate(${rotation})`);
    g.appendChild(glow);
    
    // Main train body (rectangle - like a train car)
    const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    rect.setAttribute('x', '-14');
    rect.setAttribute('y', '-6');
    rect.setAttribute('width', '28');
    rect.setAttribute('height', '12');
    rect.setAttribute('rx', '2');
    rect.setAttribute('fill', color);
    rect.setAttribute('stroke', '#ffffff');
    rect.setAttribute('stroke-width', '1.5');
    rect.setAttribute('class', 'train-body');
    rect.setAttribute('transform', `rotate(${rotation})`);
    g.appendChild(rect);
    
    // Direction indicator (small triangle at front)
    const direction = train.direction || 'backward';
    const arrow = document.createElementNS('http://www.w3.org/2000/svg', 'polygon');
    let arrowPoints;
    if (route === 'EAST' || route === 'WEST') {
      // Horizontal movement
      if ((route === 'EAST' && direction === 'forward') || (route === 'WEST' && direction === 'backward')) {
        arrowPoints = '14,-4 14,4 20,0'; // Arrow pointing right
      } else {
        arrowPoints = '-14,-4 -14,4 -20,0'; // Arrow pointing left
      }
    } else {
      // Vertical movement (North/South)
      if ((route === 'SOUTH' && direction === 'forward') || (route === 'NORTH' && direction === 'backward')) {
        arrowPoints = '-4,14 4,14 0,20'; // Arrow pointing down
      } else {
        arrowPoints = '-4,-14 4,-14 0,-20'; // Arrow pointing up
      }
    }
    arrow.setAttribute('points', arrowPoints);
    arrow.setAttribute('fill', '#ffffff');
    arrow.setAttribute('class', 'train-direction');
    g.appendChild(arrow);
    
    // Train ID label (positioned above the train)
    const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    label.setAttribute('y', route === 'NORTH' || route === 'SOUTH' ? '0' : '-16');
    label.setAttribute('x', route === 'NORTH' || route === 'SOUTH' ? '-25' : '0');
    label.setAttribute('text-anchor', route === 'NORTH' || route === 'SOUTH' ? 'end' : 'middle');
    label.setAttribute('dominant-baseline', 'middle');
    label.setAttribute('class', 'train-label');
    label.setAttribute('fill', '#ffffff');
    label.setAttribute('font-size', '10');
    label.setAttribute('font-weight', 'bold');
    label.setAttribute('font-family', 'JetBrains Mono, monospace');
    label.textContent = train.id;
    g.appendChild(label);
    
    // Add background for label for better visibility
    const labelBg = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    const labelWidth = train.id.length * 7 + 6;
    labelBg.setAttribute('x', route === 'NORTH' || route === 'SOUTH' ? (-25 - labelWidth) : (-labelWidth/2));
    labelBg.setAttribute('y', route === 'NORTH' || route === 'SOUTH' ? '-6' : '-22');
    labelBg.setAttribute('width', labelWidth);
    labelBg.setAttribute('height', '12');
    labelBg.setAttribute('rx', '2');
    labelBg.setAttribute('fill', 'rgba(0,0,0,0.7)');
    labelBg.setAttribute('class', 'train-label-bg');
    g.insertBefore(labelBg, label);
    
    // Add CLICK event listener for train details panel (instead of hover tooltip)
    g.addEventListener('click', (e) => {
      e.stopPropagation();
      showTrainDetailsPanel(train);
    });
    
    // Add hover effect for visual feedback
    g.addEventListener('mouseenter', () => {
      g.style.cursor = 'pointer';
      g.querySelector('.train-body')?.setAttribute('filter', 'brightness(1.3)');
    });
    g.addEventListener('mouseleave', () => {
      g.querySelector('.train-body')?.setAttribute('filter', '');
    });
    
    trainGroup.appendChild(g);
  });
}

// Currently selected train for details panel
let selectedTrainId = null;

// Show train details in the popup panel (click-based)
function showTrainDetailsPanel(train) {
  const panel = document.getElementById('trainDetailsPanel');
  const backdrop = document.getElementById('trainDetailsBackdrop');
  const titleEl = document.getElementById('trainDetailTitle');
  const bodyEl = document.getElementById('trainDetailsBody');
  
  if (!panel || !bodyEl) return;
  
  selectedTrainId = train.id;
  
  // Hide hover tooltip when showing popup
  hideTrainTooltip();
  
  // Get destination station name
  const destStation = getStationName(train.destination) || 'New Delhi';
  
  // Direction text
  const directionText = train.direction === 'backward' ? 
    `→ Hub (${destStation})` : 
    `← Away from Hub`;
  
  // Priority class
  const priorityClass = train.priority <= 3 ? 'priority-high' : 'priority-low';
  const priorityText = getPriorityText(train.priority);
  
  // Current power
  const currentPower = train.current_power_kw || calculateTrainPower(train);
  
  // Update title with train color
  const trainColor = train.color || TRAIN_COLORS[train.type] || '#00d4ff';
  titleEl.innerHTML = `<span style="color: ${trainColor}">🚂 ${train.id}</span>`;
  
  // Update body with train details
  bodyEl.innerHTML = `
    <div class="train-detail-row">
      <span class="train-detail-label">Name</span>
      <span class="train-detail-value">${train.name || train.type || 'Unknown'}</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Type</span>
      <span class="train-detail-value">${train.type || 'Unknown'}</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Position</span>
      <span class="train-detail-value">${(train.position || 0).toFixed(1)} km</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Speed</span>
      <span class="train-detail-value speed">${(train.speed || 0).toFixed(0)} km/h</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Direction</span>
      <span class="train-detail-value">${directionText}</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Route</span>
      <span class="train-detail-value">${train.route || 'Unknown'} Line</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Priority</span>
      <span class="train-detail-value ${priorityClass}">${priorityText}</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Mass</span>
      <span class="train-detail-value">${train.mass_tons || '--'} tons</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Power</span>
      <span class="train-detail-value">${currentPower.toFixed(0)} kW</span>
    </div>
    <div class="train-detail-row">
      <span class="train-detail-label">Destination</span>
      <span class="train-detail-value">${destStation}</span>
    </div>
  `;
  
  // Show panel and backdrop
  panel.classList.add('visible');
  if (backdrop) backdrop.classList.add('visible');
}

// Hide train details panel
function hideTrainDetailsPanel() {
  const panel = document.getElementById('trainDetailsPanel');
  const backdrop = document.getElementById('trainDetailsBackdrop');
  if (panel) {
    panel.classList.remove('visible');
    selectedTrainId = null;
    // Reset position to default (bottom-right corner)
    panel.style.top = '';
    panel.style.left = '';
    panel.style.bottom = '20px';
    panel.style.right = '20px';
  }
  if (backdrop) backdrop.classList.remove('visible');
}

// Update train details panel if a train is selected (called during simulation)
function updateTrainDetailsPanel() {
  if (!selectedTrainId) return;
  
  const train = state.trains.find(t => t.id === selectedTrainId);
  if (train) {
    showTrainDetailsPanel(train);
  }
}

// Legacy tooltip functions (kept for compatibility but not used)
function showTrainTooltip(train, event) {
  // Now using click-based panel instead
}

function moveTrainTooltip(event) {
  // Now using click-based panel instead
}

function hideTrainTooltip() {
  const tooltip = document.getElementById('trainTooltip');
  if (tooltip) {
    tooltip.classList.remove('visible');
  }
}

// Calculate train power consumption (kW) based on physics
// Formula: P = (mass * speed * 0.05) + (speed² * 0.01) + idle_power
// This gives realistic values for Indian Railways trains
function calculateTrainPower(train) {
  const massTons = train.mass_tons || 850;  // Default to Rajdhani mass
  const speed = train.speed || 0;
  const idlePower = 50;  // Base auxiliary power in kW
  
  if (speed === 0) {
    return idlePower;  // Just idle power when stopped
  }
  
  // Rolling resistance: proportional to mass and speed
  const rollingPower = massTons * speed * 0.05;
  
  // Air resistance: proportional to speed squared
  const airPower = (speed ** 2) * 0.01;
  
  return rollingPower + airPower + idlePower;
}

// Get station name from code
function getStationName(code) {
  const stationNames = {
    'NDLS': 'New Delhi',
    'DLI': 'Old Delhi Jn',
    'NRL': 'Narela',
    'SNP': 'Sonipat',
    'NZM': 'Nizamuddin',
    'FDB': 'Faridabad',
    'PWL': 'Palwal',
    'MTJ': 'Mathura Jn',
    'ANVT': 'Anand Vihar',
    'GZB': 'Ghaziabad',
    'DSB': 'Sadar Bazar',
    'DEE': 'Sarai Rohilla',
    'DEC': 'Delhi Cantt'
  };
  return stationNames[code] || code;
}

// Get priority text
function getPriorityText(priority) {
  const priorities = {
    1: 'P1 - Highest',
    2: 'P2 - High (Superfast)',
    3: 'P3 - Medium (Express)',
    4: 'P4 - Normal (Passenger)',
    5: 'P5 - Low (Local)',
    6: 'P6 - Lowest (Freight)'
  };
  return priorities[priority] || `P${priority}`;
}

// Track positions are now handled directly in interpolation functions
// West Line: Single track (y=450) from NDLS to DSB, then double track (T1=435, T2=465)
// East Line: Double track (T1=435, T2=465)
// North Line: Double track (T1=x885, T2=x915)
// South Line: Triple track (T1=x885, T2=x900, T3=x915)

// Get train position for Delhi SVG - trains move ON the track lines
function getTrainPositionDelhi(train) {
  // Detect route from train data
  const route = train.route || detectRouteFromPosition(train);
  const km = train.position || 0;
  const trackNumber = train.track || train.track_number || 1;
  
  // Get position directly from interpolation (which now handles track-specific Y positions)
  let pos;
  switch(route) {
    case 'NORTH':
      pos = interpolateNorthLine(km, trackNumber);
      break;
    case 'SOUTH':
      pos = interpolateSouthLine(km, trackNumber);
      break;
    case 'EAST':
      pos = interpolateEastLine(km, trackNumber);
      break;
    case 'WEST':
      pos = interpolateWestLine(km, trackNumber);
      break;
    default:
      pos = { x: 900, y: 450 };
  }
  
  return pos;
}

function updateTrainPositions(newTrains) {
  // Don't update from backend during execution - we control positions locally
  if (state.isExecuting) {
    return;
  }
  
  newTrains.forEach(train => {
    const marker = document.getElementById(`train-${train.id}`);
    if (marker) {
      // Use Delhi position function if using external SVG
      const pos = USE_EXTERNAL_SVG ? getTrainPositionDelhi(train) : getTrainPosition(train);
      marker.setAttribute('transform', `translate(${pos.x}, ${pos.y})`);
    } else {
      console.log(`Train marker not found for train-${train.id}`);
    }
    
    // Update state
    const idx = state.trains.findIndex(t => t.id === train.id);
    if (idx >= 0) {
      state.trains[idx] = train;
    }
  });
  
  updateTrainList();
}

function getTrainPosition(train) {
  // SVG dimensions - must match renderTrackSVG
  const isFocused = currentFocusSection !== 'all';
  const width = isFocused ? 2400 : 1800;
  const height = 350;
  const padding = { left: 80, right: 60, top: 55, bottom: 70 };
  const baseTrackSpacing = 30;
  const mergeZoneWidth = 60; // pixels - must match renderTrackSVG
  
  // ALWAYS use full route range (0 to 192 km) - no focus-based clipping
  const viewStartKm = 0;
  const viewEndKm = ROUTE.totalKm;
  
  const viewRangeKm = viewEndKm - viewStartKm;
  const trackAreaWidth = width - padding.left - padding.right;
  const trackAreaHeight = height - padding.top - padding.bottom;
  const centerY = padding.top + trackAreaHeight / 2;
  
  // Convert merge zone from pixels to km (must match SVG rendering)
  const halfMergeZoneKm = (mergeZoneWidth / 2) / trackAreaWidth * viewRangeKm;
  
  // Helper: get Y positions for tracks in a section (centered)
  const getTrackYPositions = (numTracks) => {
    const positions = [];
    const totalHeight = (numTracks - 1) * baseTrackSpacing;
    const startY = centerY - totalHeight / 2;
    for (let i = 0; i < numTracks; i++) {
      positions.push(startY + i * baseTrackSpacing);
    }
    return positions;
  };
  
  // Clamp train position to valid route bounds (0 to 192 km)
  const clampedPosition = Math.max(0, Math.min(train.position, ROUTE.totalKm));
  
  // X position based on km (scaled to view range)
  const x = padding.left + ((clampedPosition - viewStartKm) / viewRangeKm) * trackAreaWidth;
  
  // Find current section using clamped position
  const sectionIdx = ROUTE.sections.findIndex(s => clampedPosition >= s.from && clampedPosition <= s.to);
  const section = ROUTE.sections[sectionIdx >= 0 ? sectionIdx : 0] || ROUTE.sections[0];
  const trackIdx = Math.min((train.track_number || 1) - 1, section.tracks - 1);
  
  // Get Y positions for current section
  const trackYs = getTrackYPositions(section.tracks);
  let y = trackYs[trackIdx];
  
  // Check if train is in a merge zone (transitioning between sections)
  // Only check if we have a valid section index
  if (sectionIdx >= 0) {
    const prevSection = ROUTE.sections[sectionIdx - 1];
    const nextSection = ROUTE.sections[sectionIdx + 1];
    
    // Entering this section from previous (tracks changing)
    if (prevSection && prevSection.tracks !== section.tracks) {
      const distFromStart = clampedPosition - section.from;
      if (distFromStart >= 0 && distFromStart < halfMergeZoneKm) {
        const progress = distFromStart / halfMergeZoneKm;
        const prevTrackYs = getTrackYPositions(prevSection.tracks);
        const prevTrackIdx = Math.min((train.track_number || 1) - 1, prevSection.tracks - 1);
        const prevY = prevTrackYs[prevTrackIdx];
        y = prevY + (y - prevY) * progress;
      }
    }
    
    // Exiting this section to next (tracks changing)
    if (nextSection && nextSection.tracks !== section.tracks) {
      const distToEnd = section.to - clampedPosition;
      if (distToEnd >= 0 && distToEnd < halfMergeZoneKm) {
        const progress = 1 - (distToEnd / halfMergeZoneKm);
        const nextTrackYs = getTrackYPositions(nextSection.tracks);
        const nextTrackIdx = Math.min((train.track_number || 1) - 1, nextSection.tracks - 1);
        const nextY = nextTrackYs[nextTrackIdx];
        y = y + (nextY - y) * progress;
      }
    }
  }
  
  return { x, y };
}

function renderConflictZone(conflict) {
  const group = document.getElementById('conflictGroup');
  if (!group) return;
  
  const width = 1800;
  const height = 350;
  const padding = { left: 80, right: 60, top: 55, bottom: 70 };
  
  // Determine view range based on focused section
  let viewStartKm = 0;
  let viewEndKm = ROUTE.totalKm;
  
  if (currentFocusSection !== 'all') {
    const idx = parseInt(currentFocusSection);
    const section = ROUTE.sections[idx];
    if (section) {
      viewStartKm = Math.max(0, section.from - 3);
      viewEndKm = Math.min(ROUTE.totalKm, section.to + 3);
    }
  }
  
  const viewRangeKm = viewEndKm - viewStartKm;
  const trackAreaWidth = width - padding.left - padding.right;
  
  const km = conflict.position_km || 0;
  const x = padding.left + ((km - viewStartKm) / viewRangeKm) * trackAreaWidth;
  const zoneWidth = 50;
  
  group.innerHTML = `
    <!-- Glow effect -->
    <rect x="${x - zoneWidth/2 - 8}" y="${padding.top - 10}" width="${zoneWidth + 16}" height="${height - padding.top - padding.bottom + 20}" 
          class="conflict-zone-glow" rx="6"/>
    
    <!-- Main zone -->
    <rect x="${x - zoneWidth/2}" y="${padding.top}" width="${zoneWidth}" height="${height - padding.top - padding.bottom}" 
          class="conflict-zone" rx="4"/>
    
    <!-- Warning icon -->
    <text x="${x}" y="${padding.top - 15}" text-anchor="middle" font-size="18">⚠️</text>
    
    <!-- Conflict label -->
    <text x="${x}" y="${height - padding.bottom + 20}" text-anchor="middle" fill="#ff3b3b" font-size="10" font-weight="600">CONFLICT</text>
  `;
}

function clearConflictZone() {
  const group = document.getElementById('conflictGroup');
  if (group) group.innerHTML = '';
}

// ============================================
// TRAIN LIST (Dropdown)
// ============================================
function updateTrainList() {
  const container = document.getElementById('trainList');
  const countEl = document.getElementById('trainCount');
  
  countEl.textContent = state.trains.length;
  
  if (state.trains.length === 0) {
    container.innerHTML = '<div class="empty-state">Select scenario to see trains</div>';
    return;
  }
  
  container.innerHTML = state.trains.map(train => {
    const color = train.color || TRAIN_COLORS[train.type] || '#00d4ff';
    const route = train.route || detectRouteFromPosition(train);
    const routeColor = ROUTE.lines[route]?.color || color;
    
    return `
      <div class="train-item" style="border-left-color: ${color}">
        <div class="train-color" style="background: ${color}"></div>
        <div class="train-info">
          <div class="train-id">${train.id}</div>
          <div class="train-type">${train.name || train.type || 'Train'}</div>
        </div>
        <div class="train-stats">
          <span class="train-speed">${train.speed?.toFixed(0) || 0} km/h</span>
          <span class="train-pos">${train.position?.toFixed(1) || 0} km</span>
        </div>
      </div>
    `;
  }).join('');
}

// ============================================
// SCENARIO DROPDOWN (in Navbar)
// ============================================
let currentRunningScenarioId = null;  // Track which scenario is running

function renderScenarioButtons() {
  const select = document.getElementById('scenarioSelect');
  const runBtn = document.getElementById('runScenarioBtn');
  
  // Populate dropdown with scenario options
  select.innerHTML = '<option value="">Select Scenario</option>' +
    state.scenarios.map(scenario => 
      `<option value="${scenario.id}">${scenario.id}. ${scenario.name}${scenario.id === 3 ? ' 🔥' : ''}</option>`
    ).join('');
  
  // Handle selection change - just enable/disable run button
  select.addEventListener('change', () => {
    const selectedId = select.value;
    runBtn.disabled = !selectedId;
  });
  
  // Handle run button click
  runBtn.addEventListener('click', () => {
    const selectedId = select.value;
    if (selectedId) {
      currentRunningScenarioId = selectedId;  // Remember which scenario we're running
      loadScenario(parseInt(selectedId));
    }
  });
}

function updateScenarioButtons() {
  const select = document.getElementById('scenarioSelect');
  
  if (state.currentScenario && currentRunningScenarioId) {
    // Keep the dropdown showing the running scenario
    select.value = currentRunningScenarioId;
  }
}


// ============================================
// METRICS (Simplified - panels removed)
// ============================================
function updateMetrics() {
  // Panels removed - just log energy for debugging
  console.log('Energy consumed:', state.energyConsumed, 'kWh');
}

// Show sustainability impact (simplified - no panel)
function showSustainabilityImpact(solution) {
  // Panel removed - log to console
  const energySaved = state.energySaved || 0;
  if (energySaved > 0) {
    addLog('success', `💚 Energy saved: ${energySaved.toFixed(0)} kWh`);
  }
}

// Reset sustainability panel (no-op)
function resetSustainabilityPanel() {
  // Panel removed
}

// Update AI Performance (simplified)
function updateAIPerformance() {
  // Panel removed - metrics tracked in state for logging
}

// Real-time AI Performance updates during execution
function updateAIPerformanceRealTime(phase, solution) {
  // Log to console instead of updating removed panel
  switch (phase) {
    case 'approved':
      addLog('info', `⚡ Executing: ${solution?.action || 'Solution'}`);
      break;
    case 'completed':
      state.aiPerformance.conflictsResolved++;
      addLog('success', `✅ Solution applied successfully`);
      break;
    case 'temporary':
      addLog('warning', `⚠️ Temporary solution applied`);
      break;
    case 'failed':
      addLog('danger', `❌ Solution failed`);
      break;
  }
}

// ============================================
// TIME-SPACE GRAPH (Canvas)
// Distance (Y-axis) vs Time (X-axis)
// - Diagonal line = train moving
// - Horizontal line = train stopped (distance constant)
// - Steeper slope = faster train
// ============================================
let graphCtx = null;
let graphStartTime = null;
let simulationTime = 0;  // Tracks simulation time in seconds (only advances when simulation runs)
const GRAPH_PADDING = { left: 55, right: 20, top: 15, bottom: 35 };

function initGraph() {
  const canvas = document.getElementById('graphCanvas');
  if (!canvas) {
    console.error('Graph canvas not found!');
    return;
  }
  
  graphCtx = canvas.getContext('2d');
  graphStartTime = Date.now();
  simulationTime = 0;
  
  // Set canvas size after a short delay to ensure container has dimensions
  setTimeout(() => {
    resizeGraph();
    console.log('Graph initialized:', canvas.width, 'x', canvas.height);
  }, 200);
  
  // Also resize after a longer delay in case layout is still settling
  setTimeout(() => {
    resizeGraph();
  }, 500);
  
  window.addEventListener('resize', () => {
    // Reset context scale before resizing
    if (graphCtx) {
      graphCtx.setTransform(1, 0, 0, 1, 0, 0);
    }
    resizeGraph();
  });
}

function resizeGraph() {
  const canvas = document.getElementById('graphCanvas');
  if (!canvas) return;
  
  const container = canvas.parentElement;
  if (!container) return;
  
  // Get actual container dimensions
  const rect = container.getBoundingClientRect();
  const width = Math.max(rect.width - 20, 400);
  const height = Math.max(rect.height - 20, 200);
  
  // Set canvas size directly (no DPR scaling for simplicity)
  canvas.width = width;
  canvas.height = height;
  
  console.log('Graph resized:', width, 'x', height);
  drawGraph();
}

function drawGraphGrid() {
  if (!graphCtx) return;
  
  const canvas = graphCtx.canvas;
  const w = canvas.width;
  const h = canvas.height;
  const p = GRAPH_PADDING;
  const graphW = w - p.left - p.right;
  const graphH = h - p.top - p.bottom;
  
  // Clear with dark background
  graphCtx.fillStyle = '#0d1a2d';
  graphCtx.fillRect(0, 0, w, h);
  
  // Draw graph area background
  graphCtx.fillStyle = '#1a2d47';
  graphCtx.fillRect(p.left, p.top, graphW, graphH);
  
  // Grid lines
  graphCtx.strokeStyle = '#2a4060';
  graphCtx.lineWidth = 1;
  
  // Y-AXIS: Distance (0-192 km) - Station markers
  const stations = ROUTE.stations;
  stations.forEach(station => {
    const y = p.top + graphH - (station.km / ROUTE.totalKm) * graphH;
    
    // Station grid line (dashed)
    graphCtx.strokeStyle = '#3a5070';
    graphCtx.setLineDash([4, 4]);
    graphCtx.beginPath();
    graphCtx.moveTo(p.left, y);
    graphCtx.lineTo(w - p.right, y);
    graphCtx.stroke();
    graphCtx.setLineDash([]);
    
    // Station label
    graphCtx.fillStyle = '#8aa4c0';
    graphCtx.font = '10px JetBrains Mono';
    graphCtx.textAlign = 'right';
    graphCtx.fillText(station.code, p.left - 5, y + 3);
  });
  
  // Additional distance markers (every 40km)
  graphCtx.fillStyle = '#5a7a9a';
  graphCtx.font = '9px JetBrains Mono';
  for (let km = 0; km <= ROUTE.totalKm; km += 40) {
    const y = p.top + graphH - (km / ROUTE.totalKm) * graphH;
    
    // Minor grid line
    graphCtx.strokeStyle = '#2a4060';
    graphCtx.beginPath();
    graphCtx.moveTo(p.left, y);
    graphCtx.lineTo(w - p.right, y);
    graphCtx.stroke();
  }
  
  // X-AXIS: Time markers
  const timeWindow = 120; // 120 seconds visible window
  const timeInterval = 20; // Label every 20 seconds
  
  for (let t = 0; t <= timeWindow; t += timeInterval) {
    const x = p.left + (t / timeWindow) * graphW;
    
    // Vertical grid line
    graphCtx.strokeStyle = t % 60 === 0 ? '#3a5070' : '#2a4060';
    graphCtx.lineWidth = t % 60 === 0 ? 1.5 : 1;
    graphCtx.beginPath();
    graphCtx.moveTo(x, p.top);
    graphCtx.lineTo(x, h - p.bottom);
    graphCtx.stroke();
    
    // Time label
    graphCtx.fillStyle = '#8aa4c0';
    graphCtx.font = '10px JetBrains Mono';
    graphCtx.textAlign = 'center';
    const mins = Math.floor(t / 60);
    const secs = t % 60;
    graphCtx.fillText(`${mins}:${secs.toString().padStart(2, '0')}`, x, h - p.bottom + 15);
  }
  
  // Axis labels
  graphCtx.fillStyle = '#00d4ff';
  graphCtx.font = '11px Inter';
  
  // Y-axis label (Distance)
  graphCtx.save();
  graphCtx.translate(12, h / 2);
  graphCtx.rotate(-Math.PI / 2);
  graphCtx.textAlign = 'center';
  graphCtx.fillText('Distance (km)', 0, 0);
  graphCtx.restore();
  
  // X-axis label (Time)
  graphCtx.textAlign = 'center';
  graphCtx.fillText('Time (mm:ss)', w / 2, h - 3);
  
  // Draw axis lines
  graphCtx.strokeStyle = '#5a7a9a';
  graphCtx.lineWidth = 2;
  graphCtx.beginPath();
  graphCtx.moveTo(p.left, p.top);
  graphCtx.lineTo(p.left, h - p.bottom);
  graphCtx.lineTo(w - p.right, h - p.bottom);
  graphCtx.stroke();
}

function addGraphPoint(trains) {
  if (!trains || trains.length === 0) {
    console.log('addGraphPoint: No trains data');
    return;
  }
  
  // Simple approach: increment simulation time by fixed amount each call
  // This way time only advances when this function is called (i.e., when simulation runs)
  simulationTime += 0.4 * state.playbackSpeed;  // 0.4 seconds per call, scaled by speed
  
  // Store data point with simulation timestamp
  const point = {
    time: simulationTime,
    trains: trains.map(t => ({
      id: t.id,
      position: t.position,
      speed: t.speed || 0,
      color: t.color || TRAIN_COLORS[t.type] || '#00d4ff'
    }))
  };
  state.graphData.push(point);
  
  // Keep data for last 120 seconds of simulation time
  const timeWindow = 120;
  const cutoffTime = simulationTime - timeWindow;
  state.graphData = state.graphData.filter(p => p.time >= cutoffTime);
  
  drawGraph();
}

function drawGraph() {
  if (!graphCtx) {
    console.log('drawGraph: No context');
    return;
  }
  
  drawGraphGrid();
  
  const canvas = graphCtx.canvas;
  const w = canvas.width;
  const h = canvas.height;
  
  // Show message if no data, but still draw if we have at least 1 point
  if (state.graphData.length === 0) {
    graphCtx.fillStyle = '#5a7a9a';
    graphCtx.font = '14px Inter';
    graphCtx.textAlign = 'center';
    graphCtx.fillText('Select a scenario and press Play to see train movements', w / 2, h / 2);
    return;
  }
  
  const p = GRAPH_PADDING;
  const graphW = w - p.left - p.right;
  const graphH = h - p.top - p.bottom;
  
  // Time window (120 seconds)
  const timeWindow = 120;
  const latestTime = state.graphData[state.graphData.length - 1].time;
  const startTime = Math.max(0, latestTime - timeWindow);
  
  // Get unique train IDs
  const trainIds = [...new Set(state.graphData.flatMap(pt => pt.trains.map(t => t.id)))];
  
  // Draw path for each train
  trainIds.forEach(trainId => {
    const points = state.graphData
      .map(pt => {
        const train = pt.trains.find(t => t.id === trainId);
        if (!train) return null;
        
        // X: time position (scrolling window)
        const x = p.left + ((pt.time - startTime) / timeWindow) * graphW;
        
        // Y: distance position (0 at bottom, 192 at top)
        const y = p.top + graphH - (train.position / ROUTE.totalKm) * graphH;
        
        return { x, y, color: train.color, speed: train.speed };
      })
      .filter(pt => pt !== null && pt.x >= p.left);
    
    if (points.length === 0) return;
    
    // Draw train path line (if we have 2+ points)
    if (points.length >= 2) {
      graphCtx.strokeStyle = points[0].color;
      graphCtx.lineWidth = 2.5;
      graphCtx.lineCap = 'round';
      graphCtx.lineJoin = 'round';
      graphCtx.beginPath();
      graphCtx.moveTo(points[0].x, points[0].y);
      
      for (let i = 1; i < points.length; i++) {
        graphCtx.lineTo(points[i].x, points[i].y);
      }
      graphCtx.stroke();
    }
    
    // Draw current position marker (larger dot at end)
    const last = points[points.length - 1];
    
    // Glow effect
    graphCtx.fillStyle = last.color;
    graphCtx.globalAlpha = 0.3;
    graphCtx.beginPath();
    graphCtx.arc(last.x, last.y, 10, 0, Math.PI * 2);
    graphCtx.fill();
    graphCtx.globalAlpha = 1;
    
    // Main dot
    graphCtx.fillStyle = last.color;
    graphCtx.beginPath();
    graphCtx.arc(last.x, last.y, 5, 0, Math.PI * 2);
    graphCtx.fill();
    
    // Train ID label near the dot
    graphCtx.fillStyle = '#ffffff';
    graphCtx.font = 'bold 10px JetBrains Mono';
    graphCtx.textAlign = 'left';
    graphCtx.fillText(trainId, last.x + 8, last.y + 3);
  });
  
  // Draw legend showing line interpretation
  drawGraphLegend();
}

function drawGraphLegend() {
  const canvas = graphCtx.canvas;
  const w = canvas.width;
  const p = GRAPH_PADDING;
  
  // Legend box in top-right
  const legendX = w - p.right - 150;
  const legendY = p.top + 5;
  
  graphCtx.fillStyle = 'rgba(13, 26, 45, 0.9)';
  graphCtx.fillRect(legendX, legendY, 145, 45);
  graphCtx.strokeStyle = '#3a5070';
  graphCtx.lineWidth = 1;
  graphCtx.strokeRect(legendX, legendY, 145, 45);
  
  graphCtx.font = '9px Inter';
  graphCtx.fillStyle = '#8aa4c0';
  graphCtx.textAlign = 'left';
  
  // Diagonal line = moving
  graphCtx.strokeStyle = '#00d4ff';
  graphCtx.lineWidth = 2;
  graphCtx.beginPath();
  graphCtx.moveTo(legendX + 8, legendY + 18);
  graphCtx.lineTo(legendX + 28, legendY + 10);
  graphCtx.stroke();
  graphCtx.fillText('Diagonal = Moving', legendX + 35, legendY + 16);
  
  // Horizontal line = stopped
  graphCtx.beginPath();
  graphCtx.moveTo(legendX + 8, legendY + 35);
  graphCtx.lineTo(legendX + 28, legendY + 35);
  graphCtx.stroke();
  graphCtx.fillText('Horizontal = Stopped', legendX + 35, legendY + 38);
}

function clearGraph() {
  state.graphData = [];
  graphStartTime = Date.now();
  simulationTime = 0;  // Reset simulation time
  console.log('Graph cleared, simulation time reset');
  
  // Reset context transform before resizing
  if (graphCtx) {
    graphCtx.setTransform(1, 0, 0, 1, 0, 0);
  }
  resizeGraph();  // Ensure canvas is properly sized
}


// ============================================
// AI RECOMMENDATIONS PANEL
// ============================================
function showConflictModal(conflict, solutions) {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // Update status badge
  aiStatusBadge.textContent = '⚠️ CONFLICT';
  aiStatusBadge.className = 'info-badge highlight';
  aiStatusBadge.style.background = 'rgba(255, 59, 59, 0.2)';
  aiStatusBadge.style.color = '#ff3b3b';
  
  // Add alert animation
  aiContent.classList.add('ai-alert');
  
  // Get train names for display - handle both RAJ/FRT and RAJ_JPR/FRT_JPR formats
  const rajTrain = state.trains.find(t => t.id.includes('RAJ'));
  const frtTrain = state.trains.find(t => t.id.includes('FRT'));
  const rajId = rajTrain?.id || conflict.train_a || 'RAJ';
  const frtId = frtTrain?.id || conflict.train_b || 'FRT';
  
  // Get short names for display
  const rajShort = rajId.includes('_') ? rajId.split('_')[0] : rajId;
  const frtShort = frtId.includes('_') ? frtId.split('_')[0] : frtId;
  
  // Generate clear solution descriptions with multi-step logic and platform info
  const getSolutionDescription = (sol, idx) => {
    const stopTrain = sol.train_affected;
    const isRajAffected = stopTrain?.includes('RAJ');
    const isFrtAffected = stopTrain?.includes('FRT');
    
    // Get platform info from solution (from backend)
    const platformInfo = sol.platform_info || { platform_number: 9, platform_type: 'odd (less used)', station_name: 'DSB Loop' };
    const platformText = `${platformInfo.station_name || 'DSB Loop'}`;
    
    // Get train speeds from state
    const rajSpeed = rajTrain?.speed || 110;
    const frtSpeed = frtTrain?.speed || 50;
    
    if (sol.type === 'stop') {
      if (isFrtAffected) {
        // OPTION 1: Stop Freight (RECOMMENDED)
        return `<div class="solution-steps">
                  <div class="step"><span class="step-num">1</span> ${frtShort} stops at <strong>${platformText}</strong></div>
                  <div class="step"><span class="step-num">2</span> ${rajShort} continues at full speed (${rajSpeed} km/h)</div>
                  <div class="step"><span class="step-num">3</span> ${frtShort} resumes after ${rajShort} passes</div>
                </div>
                <div class="solution-verdict good">✓ Priority respected • Energy efficient • Delay to freight only</div>`;
      } else if (isRajAffected) {
        // OPTION 2: Stop Rajdhani (NOT RECOMMENDED)
        return `<div class="solution-steps">
                  <div class="step"><span class="step-num">1</span> ${rajShort} stops at <strong>${platformText}</strong></div>
                  <div class="step"><span class="step-num">2</span> ${frtShort} continues at ${frtSpeed} km/h</div>
                  <div class="step"><span class="step-num">3</span> ${rajShort} resumes after ${frtShort} passes</div>
                </div>
                <div class="solution-verdict bad">⚠️ Priority violation • Delays premium passengers • Higher energy</div>`;
      }
      return `Stop <strong>${stopTrain}</strong> at ${platformText}<br>→ Let other train pass safely`;
    } else if (sol.type === 'slow') {
      // OPTION 3: Speed Reduction
      const trainName = isRajAffected ? rajShort : frtShort;
      const speedChange = isRajAffected ? `${rajSpeed}→${Math.round(rajSpeed*0.75)} km/h` : `${frtSpeed}→${Math.round(frtSpeed*0.75)} km/h`;
      return `<div class="solution-steps">
                <div class="step"><span class="step-num">1</span> ${trainName} reduces speed (${speedChange})</div>
                <div class="step"><span class="step-num">2</span> Creates temporary separation</div>
                <div class="step"><span class="step-num">3</span> Requires precise timing coordination</div>
              </div>
              <div class="solution-verdict warning">⚠️ Temporary fix only • May need further action</div>`;
    } else if (sol.type === 'both_slow') {
      // OPTION 4: Both Slow (Coordination)
      return `<div class="solution-steps">
                <div class="step"><span class="step-num">1</span> ${rajShort}: ${rajSpeed} → ${Math.round(rajSpeed*0.85)} km/h</div>
                <div class="step"><span class="step-num">2</span> ${frtShort}: ${frtSpeed} → ${Math.round(frtSpeed*0.85)} km/h</div>
                <div class="step"><span class="step-num">3</span> Both resume after safe separation</div>
              </div>
              <div class="solution-verdict warning">⚠️ Requires precise coordination • Minimal delays</div>`;
    } else if (sol.type === 'slow_and_stop') {
      // OPTION 5: Slow + Stop (Best combination)
      return `<div class="solution-steps">
                <div class="step"><span class="step-num">1</span> ${rajShort} slows to ${Math.round(rajSpeed*0.8)} km/h (keeps moving)</div>
                <div class="step"><span class="step-num">2</span> ${frtShort} stops at <strong>DSB Loop</strong></div>
                <div class="step"><span class="step-num">3</span> ${rajShort} passes at reduced speed</div>
                <div class="step"><span class="step-num">4</span> ${frtShort} resumes journey</div>
              </div>
              <div class="solution-verdict good">✓ Best option: Energy efficient + Priority respected</div>`;
    } else if (sol.type === 'multi_step' || sol.type === 'divert') {
      // OPTION 6: Route Diversion
      return `<div class="solution-steps">
                <div class="step"><span class="step-num">1</span> Divert ${frtShort} to DSB loop track</div>
                <div class="step"><span class="step-num">2</span> ${rajShort} continues on main line</div>
                <div class="step"><span class="step-num">3</span> ${frtShort} rejoins main line after</div>
              </div>
              <div class="solution-verdict good">✓ Permanent resolution • Requires track availability</div>`;
    }
    return sol.description || sol.action;
  };
  
  // Get solution type badge
  const getSolutionBadge = (sol) => {
    if (sol.type === 'stop') return '🛑 STOP';
    if (sol.type === 'slow') return '🐢 SLOW';
    if (sol.type === 'both_slow') return '🔄 COORD';
    if (sol.type === 'slow_and_stop') return '⚡ COMBO';
    if (sol.type === 'multi_step' || sol.type === 'divert') return '🔀 DIVERT';
    return '⚡ ACTION';
  };
  
  // Render conflict info and solutions in the AI panel with CLASSIC CARD DESIGN
  aiContent.innerHTML = `
    <div class="conflict-alert-card">
      <div class="card-header danger">
        <span class="card-icon">⚠️</span>
        <div class="card-title-group">
          <h3 class="card-title">HEAD-ON COLLISION IMMINENT</h3>
          <span class="card-subtitle">West Line • ${rajShort} vs ${frtShort} • ${conflict.time_minutes?.toFixed(1) || '8'} min to impact</span>
        </div>
      </div>
    </div>
    
    <div class="ai-formula-card">
      <div class="card-header formula">
        <span class="card-icon">🧠</span>
        <h3 class="card-title">AI Decision Formula</h3>
      </div>
      <div class="card-body">
        <div class="formula-equation">Score = 60% Priority + 40% Energy</div>
        <div class="formula-scores">
          <div class="score-card raj">
            <div class="score-train-name">${rajShort}</div>
            <div class="score-priority">Priority ${rajTrain?.priority || 2}</div>
            <div class="score-value">85</div>
          </div>
          <div class="score-vs">VS</div>
          <div class="score-card frt">
            <div class="score-train-name">${frtShort}</div>
            <div class="score-priority">Priority ${frtTrain?.priority || 6}</div>
            <div class="score-value">37</div>
          </div>
        </div>
        <div class="formula-decision">→ Stop ${frtShort} (lower score), let ${rajShort} pass</div>
      </div>
    </div>
    
    <div class="solutions-container">
      <div class="solutions-header">
        <h3>🤖 AI RECOMMENDATIONS</h3>
        <span class="solutions-count">${solutions.length} options</span>
      </div>
      
      ${solutions.slice(0, 4).map((sol, idx) => `
        <div class="solution-card ${idx === 0 ? 'recommended' : ''}" data-index="${idx}">
          <div class="card-header ${idx === 0 ? 'success' : 'default'}">
            <div class="solution-rank-badge ${idx === 0 ? 'star' : ''}">${idx === 0 ? '★' : idx + 1}</div>
            <div class="solution-title-group">
              <span class="solution-type ${sol.type}">${getSolutionBadge(sol)}</span>
              ${idx === 0 ? '<span class="rec-tag">RECOMMENDED</span>' : ''}
            </div>
          </div>
          <div class="card-body">
            <div class="solution-steps-container">
              ${getSolutionDescription(sol, idx)}
            </div>
            <div class="solution-metrics-row">
              <div class="metric-item">
                <span class="metric-icon">⚡</span>
                <span class="metric-val">${sol.energy_kwh?.toFixed(0) || '--'}</span>
                <span class="metric-unit">kWh</span>
              </div>
              <div class="metric-item">
                <span class="metric-icon">⏱️</span>
                <span class="metric-val">+${sol.delay_minutes?.toFixed(0) || '8'}</span>
                <span class="metric-unit">min</span>
              </div>
              <div class="metric-item ${sol.priority_violation ? 'warning' : 'success'}">
                <span class="metric-icon">${sol.priority_violation ? '⚠️' : '✓'}</span>
                <span class="metric-val">${sol.priority_violation ? 'Priority!' : 'OK'}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="card-btn preview-btn" onclick="simulateSolution(${idx})">
              <span class="btn-icon">🎬</span> Preview
            </button>
            <button type="button" class="card-btn approve-btn" onclick="approveSolutionDirect(${idx})">
              <span class="btn-icon">✓</span> Execute
            </button>
          </div>
        </div>
      `).join('')}
      
      <button type="button" class="reject-all-card-btn" onclick="rejectAllAndShowCollision()">
        <span class="btn-icon">✕</span> Reject All & Show Collision
      </button>
    </div>
  `;
  
  // Event listeners are now handled via onclick attributes in the HTML
  console.log('showConflictModal complete - buttons should be clickable');
}

// ============================================
// REJECT ALL & COLLISION SIMULATION
// ============================================
let collisionCheckInterval = null;

function rejectAllAndShowCollision() {
  const conflict = state.conflict;
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // Get train info
  const rajTrain = state.trains.find(t => t.id?.includes('RAJ'));
  const frtTrain = state.trains.find(t => t.id?.includes('FRT'));
  
  // Update AI panel to show warning with countdown
  aiStatusBadge.textContent = '⚠️ REJECTED';
  aiStatusBadge.style.background = 'rgba(255, 184, 0, 0.3)';
  aiStatusBadge.style.color = '#ffb800';
  
  aiContent.innerHTML = `
    <div class="reject-warning">
      <div class="reject-header">
        <span class="reject-icon">⚠️</span>
        <span class="reject-title">AI RECOMMENDATIONS REJECTED</span>
      </div>
      
      <div class="reject-message">
        <div class="message-text">Section Controller has rejected all AI recommendations.</div>
        <div class="message-subtext">Trains continuing on collision course...</div>
      </div>
      
      <div class="trains-status">
        <div class="train-status-row">
          <span class="train-badge" style="background: #FF4444;">RAJ</span>
          <span class="status-text">Continuing at ${rajTrain?.speed || 50} km/h → NDLS</span>
        </div>
        <div class="train-status-row">
          <span class="train-badge" style="background: #8B4513;">FRT</span>
          <span class="status-text">Continuing at ${frtTrain?.speed || 15} km/h → DEC</span>
        </div>
      </div>
      
      <div class="collision-countdown">
        <div class="countdown-label">⏱️ Estimated time to collision:</div>
        <div class="countdown-value" id="collisionCountdown">Calculating...</div>
      </div>
      
      <div class="reject-consequence">
        <div class="consequence-title">🚨 CONSEQUENCE:</div>
        <div class="consequence-text">Without intervention, trains will collide. This demonstrates why AI recommendations should be followed.</div>
      </div>
      
      <div class="reject-actions">
        <button type="button" class="card-btn preview-btn" onclick="showConflictAgain()">
          <span class="btn-icon">🔄</span> Show Solutions Again
        </button>
        <button type="button" class="card-btn approve-btn" onclick="resetScenario()">
          <span class="btn-icon">↩️</span> Reset Scenario
        </button>
      </div>
    </div>
  `;
  
  addLog('warning', '⚠️ Controller rejected all AI recommendations!');
  addLog('info', 'Simulation resumed - trains continuing on collision course...');
  
  // Store conflict info for collision detection
  state.pendingCollision = conflict;
  
  // Resume normal simulation (same as before)
  state.isRunning = true;
  startSimulationWithCollisionCheck();
}

// Resume simulation but check for collision
function startSimulationWithCollisionCheck() {
  if (updateInterval) clearInterval(updateInterval);
  if (collisionCheckInterval) clearInterval(collisionCheckInterval);
  
  const conflict = state.pendingCollision;
  const collisionKm = conflict?.position_km || 41.7;
  const trainAId = conflict?.train_a || 'RAJ';
  const trainBId = conflict?.train_b || 'FRT';
  
  // Use shorter interval for collision detection to catch it accurately
  const baseInterval = 300; // Faster updates for collision detection
  const stepsPerInterval = Math.max(1, Math.round(state.playbackSpeed));
  
  updateInterval = setInterval(async () => {
    if (!state.isRunning) return;
    
    try {
      // At higher speeds, call API multiple times but CHECK COLLISION AFTER EACH STEP
      let data;
      let collisionDetected = false;
      
      for (let i = 0; i < stepsPerInterval; i++) {
        const res = await fetch(`${CONFIG.API_BASE}/simulation/step`, { method: 'POST' });
        data = await res.json();
        
        // Check collision after EACH step to catch it early at high speeds
        const tA = data.trains.find(t => t.id === trainAId);
        const tB = data.trains.find(t => t.id === trainBId);
        if (tA && tB && Math.abs(tA.position - tB.position) < 5) {
          collisionDetected = true;
          break; // Stop immediately when collision detected
        }
      }
      
      // Update trains with smooth transition
      updateTrainPositions(data.trains || []);
      
      // If collision detected, handle it immediately
      if (collisionDetected) {
        state.isRunning = false;
        clearInterval(updateInterval);
        updateInterval = null;
        
        const tA = data.trains.find(t => t.id === trainAId);
        const tB = data.trains.find(t => t.id === trainBId);
        const actualCollisionKm = (tA.position + tB.position) / 2;
        
        markCollisionOnGraph(actualCollisionKm);
        addLog('danger', `🚨 COLLISION at ${actualCollisionKm.toFixed(1)} km!`);
        addLog('danger', `💥 ${trainAId} & ${trainBId} collided!`);
        showCollisionEmergency({ ...conflict, position_km: actualCollisionKm });
        return;
      }
      
      // Update energy
      state.energyConsumed = data.system_energy_kwh || 0;
      updateMetrics();
      
      // Update graph
      addGraphPoint(data.trains);
      
      // Secondary collision check (backup)
      const trainA = data.trains.find(t => t.id === trainAId);
      const trainB = data.trains.find(t => t.id === trainBId);
      
      if (trainA && trainB) {
        const distance = Math.abs(trainA.position - trainB.position);
        
        // If trains are within 5km of each other, collision! (increased for better detection at high speeds)
        if (distance < 5) {
          // Stop simulation immediately
          state.isRunning = false;
          clearInterval(updateInterval);
          updateInterval = null;
          
          const actualCollisionKm = (trainA.position + trainB.position) / 2;
          
          // Mark collision on graph
          markCollisionOnGraph(actualCollisionKm);
          
          addLog('danger', `🚨 COLLISION at ${actualCollisionKm.toFixed(1)} km!`);
          addLog('danger', `💥 ${trainAId} & ${trainBId} collided!`);
          
          // Show emergency message
          showCollisionEmergency({
            ...conflict,
            position_km: actualCollisionKm
          });
          return; // Exit immediately
        }
      }
      
    } catch (error) {
      console.error('Simulation step failed:', error);
    }
  }, baseInterval);
}

// Mark collision point on the graph
function markCollisionOnGraph(collisionKm) {
  if (!graphCtx) return;
  
  const canvas = graphCtx.canvas;
  const p = GRAPH_PADDING;
  const graphH = canvas.height - p.top - p.bottom;
  
  // Y position for collision km
  const y = p.top + graphH - (collisionKm / ROUTE.totalKm) * graphH;
  
  // X position - right edge of graph (current time)
  const x = canvas.width - p.right;
  
  // Draw collision marker on graph
  graphCtx.fillStyle = '#ff3b3b';
  graphCtx.beginPath();
  graphCtx.arc(x, y, 8, 0, Math.PI * 2);
  graphCtx.fill();
  
  // Draw X mark
  graphCtx.strokeStyle = '#ffffff';
  graphCtx.lineWidth = 2;
  graphCtx.beginPath();
  graphCtx.moveTo(x - 4, y - 4);
  graphCtx.lineTo(x + 4, y + 4);
  graphCtx.moveTo(x + 4, y - 4);
  graphCtx.lineTo(x - 4, y + 4);
  graphCtx.stroke();
  
  // Label
  graphCtx.fillStyle = '#ff3b3b';
  graphCtx.font = 'bold 10px JetBrains Mono';
  graphCtx.textAlign = 'right';
  graphCtx.fillText('COLLISION', x - 12, y + 4);
}

// Show the emergency message in AI panel
function showCollisionEmergency(conflict) {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // Update status badge to EMERGENCY
  aiStatusBadge.textContent = '🚨 COLLISION';
  aiStatusBadge.style.background = 'rgba(255, 0, 0, 0.4)';
  aiStatusBadge.style.color = '#ff0000';
  
  // Stop any ongoing simulation
  state.isRunning = false;
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
  
  aiContent.innerHTML = `
    <div class="collision-emergency">
      <div class="collision-animation">
        <div class="collision-icon">💥</div>
        <div class="collision-trains">
          <span class="train-badge" style="background: #FF4444;">${conflict?.train_a || 'RAJ'}</span>
          <span class="collision-symbol">⚡</span>
          <span class="train-badge" style="background: #8B4513;">${conflict?.train_b || 'FRT'}</span>
        </div>
      </div>
      
      <div class="emergency-header">
        <span class="emergency-icon">🚨</span>
        <span class="emergency-title">COLLISION OCCURRED</span>
      </div>
      
      <div class="ai-ignored-warning">
        <div class="ignored-icon">⚠️</div>
        <div class="ignored-text">
          <strong>AI RECOMMENDATIONS WERE IGNORED</strong>
          <div class="ignored-subtext">This collision could have been prevented by following AI recommendations.</div>
        </div>
      </div>
      
      <div class="emergency-details">
        <div class="emergency-row">
          <span class="emergency-label">Location:</span>
          <span class="emergency-value">${conflict?.position_km?.toFixed(1) || '4.0'} km (West Line - DSB Section)</span>
        </div>
        <div class="emergency-row">
          <span class="emergency-label">Trains:</span>
          <span class="emergency-value">${conflict?.train_a || 'RAJ'} (Rajdhani) ↔ ${conflict?.train_b || 'FRT'} (Freight)</span>
        </div>
        <div class="emergency-row">
          <span class="emergency-label">Time:</span>
          <span class="emergency-value">${new Date().toLocaleTimeString('en-IN', { hour12: false })}</span>
        </div>
        <div class="emergency-row">
          <span class="emergency-label">Severity:</span>
          <span class="emergency-value critical">CRITICAL - HEAD-ON COLLISION</span>
        </div>
        <div class="emergency-row">
          <span class="emergency-label">Cause:</span>
          <span class="emergency-value critical">AI RECOMMENDATIONS REJECTED BY CONTROLLER</span>
        </div>
      </div>
      
      <div class="emergency-actions-required">
        <div class="action-title">⚠️ IMMEDIATE ACTIONS REQUIRED:</div>
        <div class="action-item">
          <span class="action-number">1</span>
          <span class="action-text">🚑 Call Emergency Services: <strong>112</strong></span>
        </div>
        <div class="action-item">
          <span class="action-number">2</span>
          <span class="action-text">📞 Report to Control Room: <strong>Railway Emergency</strong></span>
        </div>
        <div class="action-item">
          <span class="action-number">3</span>
          <span class="action-text">🚧 Block all tracks in section: <strong>34-54 km</strong></span>
        </div>
        <div class="action-item">
          <span class="action-number">4</span>
          <span class="action-text">📋 Notify: <strong>DRM, Station Masters, RPF</strong></span>
        </div>
        <div class="action-item">
          <span class="action-number">5</span>
          <span class="action-text">🏥 Alert nearby hospitals: <strong>Ghaziabad District Hospital</strong></span>
        </div>
      </div>
      
      <div class="emergency-footer">
        <div class="casualty-estimate">
          <span class="casualty-icon">👥</span>
          <span class="casualty-text">Estimated passengers at risk: <strong>~1000+</strong></span>
        </div>
        <button type="button" class="card-btn approve-btn" style="width: 100%; margin-top: 12px;" onclick="resetAfterCollision()">
          <span class="btn-icon">🔄</span> Reset Scenario & Try Again
        </button>
      </div>
    </div>
  `;
}

// Flash effect removed - keeping it simple for Section Controller

function resetAfterCollision() {
  // Clear any intervals first
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
  if (executionInterval) {
    clearInterval(executionInterval);
    executionInterval = null;
  }
  if (collisionCheckInterval) {
    clearInterval(collisionCheckInterval);
    collisionCheckInterval = null;
  }
  
  // Reset state
  state.conflict = null;
  state.solutions = [];
  state.isRunning = false;
  state.isExecuting = false;
  state.conflictAlreadyHandled = false;
  state.pendingCollision = null;
  
  // Reset AI panel
  resetAIPanel();
  
  // Clear conflict zone
  clearConflictZone();
  
  addLog('info', '🔄 Scenario reset. Reloading...');
  
  // Reload the current scenario
  if (state.currentScenario) {
    const scenarioId = state.currentScenario.id || 1;
    // Handle both numeric and string IDs
    const numericId = typeof scenarioId === 'string' ? parseInt(scenarioId.replace(/\D/g, '')) || 1 : scenarioId;
    loadScenario(numericId);
  }
}

function closeConflictModal() {
  resetAIPanel();
  clearConflictZone();
  state.conflict = null;
  state.conflictAlreadyHandled = false;  // Allow new conflict detection
  state.isRunning = true;
  startSimulation();
  addLog('warning', 'All solutions rejected. Simulation resumed.');
}

function resetAIPanel() {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // Reset status badge
  aiStatusBadge.textContent = 'Monitoring';
  aiStatusBadge.className = 'info-badge';
  aiStatusBadge.style.background = '';
  aiStatusBadge.style.color = '';
  
  // Remove alert animation
  aiContent.classList.remove('ai-alert');
  
  // Show scenario details if a scenario is loaded, otherwise show idle state
  if (state.currentScenario) {
    showScenarioDetails();
  } else {
    aiContent.innerHTML = `
      <div class="ai-idle-state">
        <div class="ai-icon">🧠</div>
        <div class="ai-message">AI is monitoring train movements</div>
        <div class="ai-submessage">Recommendations will appear when conflicts are detected</div>
      </div>
    `;
  }
}

function showScenarioDetails() {
  const aiContent = document.getElementById('aiContent');
  const scenario = state.currentScenario;
  
  // Get scenario-specific details
  const scenarioId = scenario?.id || scenario?.name || '';
  const isScenario1 = scenarioId.includes('energy') || scenarioId.includes('1');
  
  // Get trains info
  const rajTrain = state.trains.find(t => t.id?.includes('RAJ'));
  const frtTrain = state.trains.find(t => t.id?.includes('FRT'));
  
  // Scenario 1 specific content
  if (isScenario1 && rajTrain && frtTrain) {
    aiContent.innerHTML = `
      <div class="scenario-details">
        <div class="scenario-header">
          <span class="scenario-icon">🎯</span>
          <div class="scenario-title">
            <div class="scenario-name">Energy Sustainability Demo</div>
            <div class="scenario-section">West Line • Head-on Collision Scenario</div>
          </div>
        </div>
        
        <div class="scenario-description">
          <div class="desc-title">📋 SCENARIO OVERVIEW</div>
          <div class="desc-text">
            Two trains approaching each other on <strong>single track section</strong> of West Line.
            AI must decide which train to stop at <strong>DSB Holding Loop</strong> (4 km from NDLS).
          </div>
        </div>
        
        <div class="trains-involved">
          <div class="section-title">🚂 TRAINS INVOLVED</div>
          <div class="train-card raj">
            <div class="train-header">
              <span class="train-badge" style="background: #FF4444;">RAJ</span>
              <span class="train-name">Jaipur Rajdhani</span>
              <span class="priority-badge p2">P2</span>
            </div>
            <div class="train-stats">
              <div class="stat"><span class="label">Position:</span> <span class="value">${rajTrain.position?.toFixed(1)} km</span></div>
              <div class="stat"><span class="label">Speed:</span> <span class="value">${rajTrain.speed} km/h</span></div>
              <div class="stat"><span class="label">Direction:</span> <span class="value">→ NDLS</span></div>
              <div class="stat"><span class="label">Passengers:</span> <span class="value">1,100</span></div>
            </div>
          </div>
          <div class="train-card frt">
            <div class="train-header">
              <span class="train-badge" style="background: #8B4513;">FRT</span>
              <span class="train-name">Jaipur Freight</span>
              <span class="priority-badge p6">P6</span>
            </div>
            <div class="train-stats">
              <div class="stat"><span class="label">Position:</span> <span class="value">${frtTrain.position?.toFixed(1)} km</span></div>
              <div class="stat"><span class="label">Speed:</span> <span class="value">${frtTrain.speed} km/h</span></div>
              <div class="stat"><span class="label">Direction:</span> <span class="value">→ DEC</span></div>
              <div class="stat"><span class="label">Cargo:</span> <span class="value">4,200 tons Coal</span></div>
            </div>
          </div>
        </div>
        
        <div class="decision-preview">
          <div class="section-title">🧠 AI DECISION FORMULA</div>
          <div class="formula-box">
            <div class="formula">Final Score = <strong>60% Priority</strong> + <strong>40% Energy</strong></div>
            <div class="formula-note">Higher priority trains get preference. Energy efficiency is secondary.</div>
          </div>
        </div>
        
        <div class="why-section">
          <div class="section-title">❓ WHY ONLY 2 TRAINS?</div>
          <div class="why-text">
            This scenario focuses on the <strong>core decision logic</strong>:
            <ul>
              <li>Clear head-on collision on single track</li>
              <li>Priority vs Energy trade-off demonstration</li>
              <li>DSB Holding Loop as resolution point</li>
            </ul>
            <em>Other scenarios (2-5) show multi-train cascades, loop utilization, and emergencies.</em>
          </div>
        </div>
        
        <div class="ai-monitoring active">
          <div class="monitoring-icon pulse">🧠</div>
          <div class="monitoring-text">AI is monitoring... Conflict will be detected when both trains approach DSB (4 km)</div>
        </div>
      </div>
    `;
  } else {
    // Generic scenario display for other scenarios
    const trainsByRoute = {
      NORTH: state.trains.filter(t => t.route === 'NORTH'),
      SOUTH: state.trains.filter(t => t.route === 'SOUTH'),
      EAST: state.trains.filter(t => t.route === 'EAST'),
      WEST: state.trains.filter(t => t.route === 'WEST')
    };
    
    const routeColors = {
      NORTH: '#00ff88',
      SOUTH: '#00d4ff',
      EAST: '#ff6b35',
      WEST: '#a855f7'
    };
    
    aiContent.innerHTML = `
      <div class="scenario-details">
        <div class="scenario-header">
          <span class="scenario-icon">🎯</span>
          <div class="scenario-title">
            <div class="scenario-name">${scenario?.name || 'Delhi Hub Scenario'}</div>
            <div class="scenario-section">${scenario?.description || '4 Routes Converging on New Delhi'}</div>
          </div>
        </div>
        
        <div class="conflict-zone-info">
          <div class="section-title">🚉 SCENARIO INFO</div>
          <div class="zone-detail">
            <span class="zone-label">Route:</span>
            <span class="zone-value">${scenario?.route || 'MULTI'} Line</span>
          </div>
          <div class="zone-detail">
            <span class="zone-label">Trains:</span>
            <span class="zone-value">${state.trains.length} Active</span>
          </div>
          <div class="zone-detail">
            <span class="zone-label">Focus:</span>
            <span class="zone-value">${scenario?.demo_focus || 'Conflict Resolution'}</span>
          </div>
        </div>
        
        ${Object.entries(trainsByRoute).filter(([route, trains]) => trains.length > 0).map(([route, trains]) => `
          <div class="trains-section">
            <div class="section-title" style="color: ${routeColors[route]}">
              ${route === 'NORTH' ? '⬆️' : route === 'SOUTH' ? '⬇️' : route === 'EAST' ? '➡️' : '⬅️'} 
              ${route} LINE
            </div>
            ${trains.map(t => `
              <div class="train-item">
                <div class="train-badge" style="background: ${t.color || '#00d4ff'};">${t.id.split('_')[0]}</div>
                <div class="train-details">
                  <div class="train-name">${t.name || t.type}</div>
                  <div class="train-info">P${t.priority} • ${t.speed} km/h • ${t.position?.toFixed(0)} km</div>
                </div>
              </div>
            `).join('')}
          </div>
        `).join('')}
        
        <div class="ai-monitoring active">
          <div class="monitoring-icon pulse">🧠</div>
          <div class="monitoring-text">AI is monitoring train movements. Conflicts will be detected automatically.</div>
        </div>
      </div>
    `;
  }
}

// ============================================
// SIMULATION MODAL
// ============================================
let currentSimulationIndex = null;
let simulationAnimationId = null;

// Helper to get station name from km (Delhi Section)
function getStationNameFromKm(km) {
  const stationMap = {
    0: 'New Delhi', 5: 'Nizamuddin', 7: 'Old Delhi', 12: 'Anand Vihar',
    25: 'Faridabad/Ghaziabad', 32: 'Narela', 42: 'Sonipat', 60: 'Palwal', 141: 'Mathura'
  };
  // Find closest station
  let closest = 'Station';
  let minDist = Infinity;
  for (const [stationKm, name] of Object.entries(stationMap)) {
    const dist = Math.abs(km - parseInt(stationKm));
    if (dist < minDist) {
      minDist = dist;
      closest = name;
    }
  }
  return closest;
}

// Simulation state
let simSpeed = 1;
let simAnimationId = null;

function simulateSolution(index) {
  currentSimulationIndex = index;
  const solution = state.solutions[index];
  const conflict = state.conflict;
  
  // Open simulation modal
  const modal = document.getElementById('simulationModal');
  document.getElementById('simAction').textContent = solution.action;
  document.getElementById('simEnergy').textContent = `${solution.energy_kwh?.toFixed(0)} kWh`;
  document.getElementById('simDelay').textContent = `+${solution.delay_minutes?.toFixed(1)} min to ${solution.train_affected}`;
  
  // Set up speed controls
  document.querySelectorAll('.sim-speed-btn').forEach(btn => {
    btn.classList.remove('active');
    if (parseFloat(btn.dataset.speed) === simSpeed) {
      btn.classList.add('active');
    }
    btn.onclick = () => {
      simSpeed = parseFloat(btn.dataset.speed);
      document.querySelectorAll('.sim-speed-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    };
  });
  
  // Restart button
  document.getElementById('restartSimulation').onclick = () => {
    if (simAnimationId) cancelAnimationFrame(simAnimationId);
    drawSimulationGraph(solution, conflict);
  };
  
  // Update status based on solution type
  updateSimulationStatus(solution);
  
  modal.classList.add('active');
  
  // Draw time-space graph simulation
  drawSimulationGraph(solution, conflict);
}

function updateSimulationStatus(solution) {
  const statusEl = document.getElementById('simStatus');
  
  if (solution.type === 'stop') {
    statusEl.className = 'simulation-status success';
    statusEl.innerHTML = `✓ <strong>${solution.train_affected}</strong> stops and waits. <strong>${solution.train_passing || 'Other train'}</strong> passes safely. Conflict permanently resolved.`;
  } else if (solution.type === 'slow') {
    statusEl.className = 'simulation-status warning';
    statusEl.innerHTML = `⚠️ <strong>${solution.train_affected}</strong> slows down. This only DELAYS the conflict - may need further action.`;
  } else if (solution.type === 'both_slow') {
    statusEl.className = 'simulation-status warning';
    statusEl.innerHTML = `⚠️ Both trains slow down. This is a TEMPORARY measure - conflict is delayed, not resolved.`;
  } else {
    statusEl.className = 'simulation-status info';
    statusEl.innerHTML = `ℹ️ Multi-step solution in progress.`;
  }
}

// Draw time-space graph showing the solution
function drawSimulationGraph(solution, conflict) {
  const canvas = document.getElementById('simulationCanvas');
  if (!canvas) {
    console.error('Simulation canvas not found');
    return;
  }
  
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;
  
  // Graph padding
  const p = { left: 60, right: 30, top: 30, bottom: 50 };
  const graphW = w - p.left - p.right;
  const graphH = h - p.top - p.bottom;
  
  // Time range: 0 to 15 minutes
  const timeRange = 15; // minutes
  
  // Distance range: West Line section (0-16 km)
  const distMin = 0;
  const distMax = 16;
  const distRange = distMax - distMin;
  
  // Get train data - find RAJ and FRT specifically
  const rajTrain = state.trains.find(t => t.id?.includes('RAJ'));
  const frtTrain = state.trains.find(t => t.id?.includes('FRT'));
  const stoppedTrainId = solution.train_affected;
  
  // Current positions (West Line: RAJ at 15km going backward, FRT at 0km going forward)
  const rajPos = rajTrain?.position || 15;
  const frtPos = frtTrain?.position || 0;
  const rajSpeed = rajTrain?.speed || 50; // km/h
  const frtSpeed = frtTrain?.speed || 15;  // km/h
  
  // DSB (Sadar Bazar) is at 4km - the holding loop
  const DSB_KM = 4;
  
  // Conflict point (where they would meet)
  const conflictKm = conflict?.position_km || 4;
  
  console.log('Drawing simulation graph:', { rajPos, frtPos, rajSpeed, frtSpeed, stoppedTrainId });
  
  // Helper functions
  const timeToX = (t) => p.left + (t / timeRange) * graphW;
  const kmToY = (km) => p.top + graphH - ((km - distMin) / distRange) * graphH;
  
  // Animation with speed control
  let animProgress = 0;
  const baseAnimDuration = 6000; // 6 seconds at 1x speed
  let startTime = Date.now();
  
  function draw() {
    const elapsed = (Date.now() - startTime) * simSpeed; // Apply speed multiplier
    animProgress = Math.min(elapsed / baseAnimDuration, 1);
    
    // Clear canvas
    ctx.fillStyle = '#0d1a2d';
    ctx.fillRect(0, 0, w, h);
    
    // Draw grid
    ctx.strokeStyle = '#2a4060';
    ctx.lineWidth = 1;
    
    // Vertical grid (time)
    for (let t = 0; t <= timeRange; t += 3) {
      const x = timeToX(t);
      ctx.beginPath();
      ctx.moveTo(x, p.top);
      ctx.lineTo(x, h - p.bottom);
      ctx.stroke();
      
      // Time label
      ctx.fillStyle = '#8aa4c0';
      ctx.font = '10px JetBrains Mono';
      ctx.textAlign = 'center';
      ctx.fillText(`${t}m`, x, h - p.bottom + 15);
    }
    
    // Horizontal grid (distance) with station markers - West Line
    const stations = [
      { km: 0, name: 'NDLS (New Delhi)' },
      { km: 4, name: 'DSB (Sadar Bazar) - Loop' },
      { km: 10, name: 'DEE (Sarai Rohilla)' },
      { km: 15, name: 'DEC (Delhi Cantt)' }
    ];
    
    for (let km = distMin; km <= distMax; km += 4) {
      const y = kmToY(km);
      ctx.strokeStyle = '#2a4060';
      ctx.beginPath();
      ctx.moveTo(p.left, y);
      ctx.lineTo(w - p.right, y);
      ctx.stroke();
      
      // Distance label
      ctx.fillStyle = '#8aa4c0';
      ctx.font = '10px JetBrains Mono';
      ctx.textAlign = 'right';
      ctx.fillText(`${km}`, p.left - 5, y + 4);
    }
    
    // Station markers
    stations.forEach(st => {
      if (st.km >= distMin && st.km <= distMax) {
        const y = kmToY(st.km);
        ctx.strokeStyle = '#5a7a9a';
        ctx.setLineDash([3, 3]);
        ctx.beginPath();
        ctx.moveTo(p.left, y);
        ctx.lineTo(w - p.right, y);
        ctx.stroke();
        ctx.setLineDash([]);
        
        if (st.name) {
          ctx.fillStyle = '#00d4ff';
          ctx.font = '9px JetBrains Mono';
          ctx.textAlign = 'left';
          ctx.fillText(st.name, p.left + 5, y - 5);
        }
      }
    });
    
    // Conflict zone marker
    const conflictY = kmToY(conflictKm);
    ctx.fillStyle = 'rgba(255, 59, 59, 0.1)';
    ctx.fillRect(p.left, conflictY - 10, graphW, 20);
    
    // Axis labels
    ctx.fillStyle = '#00d4ff';
    ctx.font = '11px Inter';
    ctx.textAlign = 'center';
    ctx.fillText('Time (minutes)', w / 2, h - 5);
    
    ctx.save();
    ctx.translate(12, h / 2);
    ctx.rotate(-Math.PI / 2);
    ctx.fillText('Distance (km)', 0, 0);
    ctx.restore();
    
    // Only show RAJ and FRT in simulation graph (no other trains)
    // West Line: RAJ starts at 15km going backward (decreasing km), FRT starts at 0km going forward (increasing km)
    
    // Calculate speeds in km per minute
    const rajSpeedPerMin = rajSpeed / 60; // km per minute (RAJ going backward = decreasing km)
    const frtSpeedPerMin = frtSpeed / 60; // km per minute (FRT going forward = increasing km)
    
    // Time when trains would collide (without intervention)
    // RAJ at 15km going down, FRT at 0km going up, they meet somewhere in between
    const collisionTime = (rajPos - frtPos) / (rajSpeedPerMin + frtSpeedPerMin);
    const collisionPoint = frtPos + frtSpeedPerMin * collisionTime;
    
    // ========== DRAW ORIGINAL PATHS (DASHED - COLLISION COURSE) ==========
    
    // Original RAJ path (dashed red) - RAJ going backward (15km → 0km)
    ctx.strokeStyle = 'rgba(255, 68, 68, 0.4)';
    ctx.lineWidth = 2;
    ctx.setLineDash([5, 5]);
    ctx.beginPath();
    for (let t = 0; t <= timeRange; t += 0.3) {
      const km = rajPos - rajSpeedPerMin * t; // RAJ going backward (decreasing km)
      if (km < distMin) break;
      const x = timeToX(t);
      const y = kmToY(km);
      if (t === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
    
    // Original FRT path (dashed brown) - FRT going forward (0km → 15km)
    ctx.strokeStyle = 'rgba(205, 133, 63, 0.4)';
    ctx.beginPath();
    for (let t = 0; t <= timeRange; t += 0.3) {
      const km = frtPos + frtSpeedPerMin * t; // FRT going forward (increasing km)
      if (km > distMax) break;
      const x = timeToX(t);
      const y = kmToY(km);
      if (t === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
    ctx.setLineDash([]); // Reset dash
    
    // Mark collision point on original paths
    if (collisionTime > 0 && collisionTime < timeRange) {
      const collisionX = timeToX(collisionTime);
      const collisionY = kmToY(collisionPoint);
      ctx.fillStyle = 'rgba(255, 0, 0, 0.3)';
      ctx.beginPath();
      ctx.arc(collisionX, collisionY, 8, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#ff0000';
      ctx.font = '9px JetBrains Mono';
      ctx.textAlign = 'center';
      ctx.fillText('COLLISION', collisionX, collisionY - 12);
    }
    
    // ========== DRAW NEW PATHS (SOLID - AFTER SOLUTION) ==========
    // West Line: RAJ going backward (15→0), FRT going forward (0→15)
    // DSB (Sadar Bazar) at 4km is the holding loop
    
    const animEndTime = timeRange * animProgress;
    
    // Determine which train is affected
    const isRajStopped = solution.type === 'stop' && stoppedTrainId?.includes('RAJ');
    const isFrtStopped = solution.type === 'stop' && stoppedTrainId?.includes('FRT');
    const isRajSlowed = solution.type === 'slow' && stoppedTrainId?.includes('RAJ');
    const isFrtSlowed = solution.type === 'slow' && stoppedTrainId?.includes('FRT');
    const isBothSlowed = solution.type === 'both_slow';
    
    // Time for FRT to reach DSB (4km) from 0km
    const timeToReachDSB = DSB_KM / frtSpeedPerMin;
    
    // Time for RAJ to pass DSB (from 15km to 4km)
    const timeRajPassesDSB = (rajPos - DSB_KM) / rajSpeedPerMin;
    
    // ===== DRAW RAJ PATH (solid red) =====
    ctx.strokeStyle = '#ff4444';
    ctx.lineWidth = 3;
    ctx.beginPath();
    
    for (let t = 0; t <= animEndTime; t += 0.2) {
      let km;
      
      if (isRajStopped) {
        // RAJ stops at DSB and waits for FRT to pass
        // 0-2 min: RAJ moves toward DSB
        // 2-8 min: RAJ STOPPED at DSB (FLAT LINE)
        // 8+ min: RAJ resumes
        const timeToStop = 2;
        const stopDuration = 6;
        if (t < timeToStop) {
          km = rajPos - rajSpeedPerMin * t;
        } else if (t < timeToStop + stopDuration) {
          km = rajPos - rajSpeedPerMin * timeToStop; // FLAT LINE - stopped
        } else {
          km = (rajPos - rajSpeedPerMin * timeToStop) - rajSpeedPerMin * (t - timeToStop - stopDuration);
        }
      } else if (isRajSlowed || isBothSlowed) {
        // RAJ slowed by 25%
        const slowedSpeed = rajSpeedPerMin * 0.75;
        km = rajPos - slowedSpeed * t;
      } else {
        // RAJ continues at normal speed
        km = rajPos - rajSpeedPerMin * t;
      }
      
      if (km < distMin) break;
      
      const x = timeToX(t);
      const y = kmToY(km);
      if (t === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
    
    // ===== DRAW FRT PATH (solid brown) =====
    ctx.strokeStyle = '#cd853f';
    ctx.lineWidth = 3;
    ctx.beginPath();
    
    for (let t = 0; t <= animEndTime; t += 0.2) {
      let km;
      
      if (isFrtStopped) {
        // FRT stops at DSB and waits for RAJ to pass
        // 0-3 min: FRT moves toward DSB (0→4km)
        // 3-10 min: FRT STOPPED at DSB (FLAT LINE) - waiting for RAJ
        // 10+ min: FRT resumes
        const timeToStop = Math.min(timeToReachDSB, 3);
        const stopDuration = 7;
        if (t < timeToStop) {
          km = frtPos + frtSpeedPerMin * t;
        } else if (t < timeToStop + stopDuration) {
          km = Math.min(frtPos + frtSpeedPerMin * timeToStop, DSB_KM); // FLAT LINE - stopped at DSB
        } else {
          km = DSB_KM + frtSpeedPerMin * (t - timeToStop - stopDuration);
        }
      } else if (isFrtSlowed || isBothSlowed) {
        // FRT slowed by 25%
        const slowedSpeed = frtSpeedPerMin * 0.75;
        km = frtPos + slowedSpeed * t;
      } else {
        // FRT continues at normal speed
        km = frtPos + frtSpeedPerMin * t;
      }
      
      if (km > distMax) break;
      
      const x = timeToX(t);
      const y = kmToY(km);
      if (t === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
    
    // Draw train labels at current animated position
    const currentTime = timeRange * animProgress;
    
    // Calculate RAJ current position based on solution
    let rajCurrentKm;
    if (isRajStopped) {
      const timeToStop = 2;
      const stopDuration = 6;
      if (currentTime < timeToStop) {
        rajCurrentKm = rajPos - rajSpeedPerMin * currentTime;
      } else if (currentTime < timeToStop + stopDuration) {
        rajCurrentKm = rajPos - rajSpeedPerMin * timeToStop;
      } else {
        rajCurrentKm = (rajPos - rajSpeedPerMin * timeToStop) - rajSpeedPerMin * (currentTime - timeToStop - stopDuration);
      }
    } else if (isRajSlowed || isBothSlowed) {
      rajCurrentKm = rajPos - (rajSpeedPerMin * 0.75) * currentTime;
    } else {
      rajCurrentKm = rajPos - rajSpeedPerMin * currentTime;
    }
    
    // Draw RAJ label
    if (rajCurrentKm <= distMax && rajCurrentKm >= distMin) {
      ctx.fillStyle = '#ff4444';
      ctx.font = 'bold 11px JetBrains Mono';
      ctx.textAlign = 'left';
      ctx.fillText('RAJ', timeToX(currentTime) + 8, kmToY(rajCurrentKm) - 8);
      // Draw marker dot
      ctx.beginPath();
      ctx.arc(timeToX(currentTime), kmToY(rajCurrentKm), 5, 0, Math.PI * 2);
      ctx.fill();
    }
    
    // Calculate FRT current position based on solution
    let frtCurrentKm;
    if (isFrtStopped) {
      const timeToStop = Math.min(timeToReachDSB, 3);
      const stopDuration = 7;
      if (currentTime < timeToStop) {
        frtCurrentKm = frtPos + frtSpeedPerMin * currentTime;
      } else if (currentTime < timeToStop + stopDuration) {
        frtCurrentKm = Math.min(frtPos + frtSpeedPerMin * timeToStop, DSB_KM);
      } else {
        frtCurrentKm = DSB_KM + frtSpeedPerMin * (currentTime - timeToStop - stopDuration);
      }
    } else if (isFrtSlowed || isBothSlowed) {
      frtCurrentKm = frtPos + (frtSpeedPerMin * 0.75) * currentTime;
    } else {
      frtCurrentKm = frtPos + frtSpeedPerMin * currentTime;
    }
    
    // Draw FRT label
    if (frtCurrentKm <= distMax && frtCurrentKm >= distMin) {
      ctx.fillStyle = '#cd853f';
      ctx.font = 'bold 11px JetBrains Mono';
      ctx.textAlign = 'left';
      ctx.fillText('FRT', timeToX(currentTime) + 8, kmToY(frtCurrentKm) + 15);
      // Draw marker dot
      ctx.beginPath();
      ctx.arc(timeToX(currentTime), kmToY(frtCurrentKm), 5, 0, Math.PI * 2);
      ctx.fill();
    }
    
    // Time indicator
    ctx.fillStyle = '#ffffff';
    ctx.font = '11px JetBrains Mono';
    ctx.textAlign = 'center';
    ctx.fillText(`Time: ${(currentTime).toFixed(1)} min`, w / 2, p.top + 12);
    
    // Draw legend (top right)
    const legendX = w - p.right - 120;
    const legendY = p.top + 10;
    
    ctx.fillStyle = 'rgba(13, 26, 45, 0.9)';
    ctx.fillRect(legendX - 5, legendY - 5, 125, 70);
    ctx.strokeStyle = '#2a4060';
    ctx.lineWidth = 1;
    ctx.strokeRect(legendX - 5, legendY - 5, 125, 70);
    
    ctx.font = '9px JetBrains Mono';
    ctx.textAlign = 'left';
    
    // Dashed line = original path
    ctx.setLineDash([4, 4]);
    ctx.strokeStyle = '#888';
    ctx.beginPath();
    ctx.moveTo(legendX, legendY + 8);
    ctx.lineTo(legendX + 25, legendY + 8);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.fillStyle = '#aaa';
    ctx.fillText('Original (collision)', legendX + 30, legendY + 12);
    
    // Solid line = new path
    ctx.strokeStyle = '#888';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(legendX, legendY + 25);
    ctx.lineTo(legendX + 25, legendY + 25);
    ctx.stroke();
    ctx.fillStyle = '#fff';
    ctx.fillText('After solution', legendX + 30, legendY + 29);
    
    // RAJ color
    ctx.fillStyle = '#ff4444';
    ctx.fillRect(legendX, legendY + 38, 12, 12);
    ctx.fillStyle = '#ff4444';
    ctx.fillText('RAJ (Rajdhani)', legendX + 18, legendY + 48);
    
    // FRT color
    ctx.fillStyle = '#cd853f';
    ctx.fillRect(legendX, legendY + 52, 12, 12);
    ctx.fillStyle = '#cd853f';
    ctx.fillText('FRT (Freight)', legendX + 18, legendY + 62);
    
    // Continue animation with speed control
    if (animProgress < 1) {
      simAnimationId = requestAnimationFrame(draw);
    }
  }
  
  // Cancel any existing animation
  if (simAnimationId) {
    cancelAnimationFrame(simAnimationId);
  }
  
  draw();
}

function closeSimulationModal() {
  // Cancel any running animation
  if (simulationAnimationId) {
    cancelAnimationFrame(simulationAnimationId);
    simulationAnimationId = null;
  }
  
  document.getElementById('simulationModal').classList.remove('active');
  
  // Reopen conflict modal by showing solutions in AI panel
  if (state.conflict && state.solutions.length > 0) {
    showConflictModal(state.conflict, state.solutions);
  }
}

function approveSolution() {
  if (currentSimulationIndex !== null) {
    approveSolutionDirect(currentSimulationIndex);
  }
  document.getElementById('simulationModal').classList.remove('active');
}

function approveSolutionDirect(index) {
  console.log('approveSolutionDirect called with index:', index);
  console.log('state.solutions:', state.solutions);
  console.log('state.conflict:', state.conflict);
  
  const solution = state.solutions[index];
  const conflict = state.conflict;
  
  if (!solution) {
    console.error('No solution found at index:', index);
    addLog('error', `Failed to approve: No solution at index ${index}`);
    return;
  }
  
  if (!conflict) {
    console.error('No conflict in state');
    addLog('error', 'Failed to approve: No active conflict');
    return;
  }
  
  // Close modals
  const conflictModal = document.getElementById('conflictModal');
  const simulationModal = document.getElementById('simulationModal');
  if (conflictModal) conflictModal.classList.remove('active');
  if (simulationModal) simulationModal.classList.remove('active');
  
  addLog('success', `✅ Solution approved: ${solution.action}`);
  addLog('info', `Energy: ${solution.energy_kwh?.toFixed(0)} kWh | Delay: ${solution.delay_minutes?.toFixed(1)} min`);
  
  // Update AI Performance - Solution approved
  state.aiPerformance.solutionApproved = solution.action;
  state.aiPerformance.solutionType = solution.type;
  state.aiPerformance.executionStartTime = Date.now();
  updateAIPerformanceRealTime('approved', solution);
  
  // Update energy tracking
  updateEnergyOnSolutionApproved(solution);
  
  // Apply the solution - show resolution animation
  showSolutionExecution(solution, conflict);
}

// Show the solution being executed with ACTUAL track visualization
function showSolutionExecution(solution, conflict) {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  if (!aiContent) {
    console.error('aiContent element not found!');
    return;
  }
  
  // Store execution state
  state.executingSolution = solution;
  state.executionPhase = 1;
  state.executionPaused = false;
  
  const trainAffected = solution.train_affected || 'FRT';
  const otherTrain = trainAffected === 'FRT' ? 'RAJ' : 'FRT';
  
  // Update status
  aiStatusBadge.textContent = '⚡ EXECUTING';
  aiStatusBadge.style.background = 'rgba(0, 255, 136, 0.2)';
  aiStatusBadge.style.color = '#00ff88';
  
  // Generate dynamic step descriptions based on solution type
  const solutionType = solution.type || 'stop';
  const stopTrain = trainAffected;
  const passTrain = otherTrain;
  
  let steps = [];
  let platformInfo = '';
  
  // West Line: DSB (Sadar Bazar) at 4km is the holding loop
  if (solutionType === 'stop' && (stopTrain === 'FRT' || stopTrain?.includes('FRT'))) {
    steps = [
      `FRT approaching DSB Holding Loop (4 km)...`,
      `FRT stopping at DSB Loop - waiting for RAJ to pass...`,
      `RAJ passing DSB at full speed → NDLS...`,
      `✅ Scenario Complete - Collision Avoided!`
    ];
    platformInfo = `DSB (Sadar Bazar) - <strong>Holding Loop</strong>`;
  } else if (solutionType === 'stop' && (stopTrain === 'RAJ' || stopTrain?.includes('RAJ'))) {
    steps = [
      `RAJ approaching DSB Holding Loop (4 km)...`,
      `RAJ stopping at DSB Loop - waiting for FRT to pass...`,
      `FRT passing DSB → DEC (Delhi Cantt)...`,
      `✅ Scenario Complete - Collision Avoided!`
    ];
    platformInfo = `DSB (Sadar Bazar) - <strong>Holding Loop</strong>`;
  } else if (solutionType === 'slow') {
    steps = [
      `${stopTrain} reducing speed by 25%...`,
      `Both trains continuing at adjusted speeds...`,
      `Trains passing with safe separation...`,
      `✅ Scenario Complete - Collision Avoided!`
    ];
    platformInfo = `No stop required - Speed reduction only`;
  } else if (solutionType === 'both_slow') {
    steps = [
      `Both trains reducing speed by 15%...`,
      `RAJ at 42 km/h, FRT at 12 km/h...`,
      `Trains passing with increased separation...`,
      `✅ Scenario Complete - Collision Avoided!`
    ];
    platformInfo = `No stop required - Both trains slowed`;
  } else {
    // Default - stop FRT
    steps = [
      `FRT approaching DSB Holding Loop...`,
      `FRT stopping at DSB Loop...`,
      `RAJ passing safely...`,
      `✅ Scenario Complete - Collision Avoided!`
    ];
    platformInfo = `DSB (Sadar Bazar) - Holding Loop`;
  }
  
  // Show execution panel with pause button
  aiContent.innerHTML = `
    <div class="solution-execution">
      <div class="execution-header">
        <span class="execution-icon">⚡</span>
        <span class="execution-title">EXECUTING: ${solution.action || 'Solution'}</span>
      </div>
      
      <div class="execution-controls">
        <button type="button" class="btn btn-secondary" id="pauseExecutionBtn" onclick="toggleExecutionPause()">
          ⏸️ Pause (for explanation)
        </button>
      </div>
      
      <div class="execution-steps">
        <div class="step-item active" id="step1">
          <span class="step-number">1</span>
          <span class="step-text">${steps[0]}</span>
          <span class="step-status">⏳</span>
        </div>
        <div class="step-item" id="step2">
          <span class="step-number">2</span>
          <span class="step-text">${steps[1]}</span>
          <span class="step-status">⏳</span>
        </div>
        <div class="step-item" id="step3">
          <span class="step-number">3</span>
          <span class="step-text">${steps[2]}</span>
          <span class="step-status">⏳</span>
        </div>
        <div class="step-item" id="step4">
          <span class="step-number">4</span>
          <span class="step-text">${steps[3]}</span>
          <span class="step-status">⏳</span>
        </div>
      </div>
      
      <div class="execution-progress">
        <div class="progress-bar" id="executionProgress"></div>
      </div>
      
      <div class="platform-info">
        <div class="platform-label">🚉 Action:</div>
        <div class="platform-value">${platformInfo}</div>
      </div>
      
      <div class="execution-train-status">
        <div class="train-status-item" id="rajStatus">
          <span class="train-name">🚄 RAJ:</span>
          <span class="train-speed" id="rajSpeedDisplay">50 km/h</span>
          <span class="train-pos" id="rajPosDisplay">-- km</span>
        </div>
        <div class="train-status-item" id="frtStatus">
          <span class="train-name">🚂 FRT:</span>
          <span class="train-speed" id="frtSpeedDisplay">15 km/h</span>
          <span class="train-pos" id="frtPosDisplay">-- km</span>
          <span class="train-platform" id="platformDisplay">--</span>
        </div>
      </div>
    </div>
  `;
  
  addLog('info', `📡 Executing solution on track...`);
  
  // Start the actual execution on track
  startTrackExecution(solution, trainAffected, otherTrain);
}

// Toggle pause during execution (for explaining to judges)
function toggleExecutionPause() {
  state.executionPaused = !state.executionPaused;
  const btn = document.getElementById('pauseExecutionBtn');
  if (btn) {
    if (state.executionPaused) {
      btn.innerHTML = '▶️ Resume Execution';
      btn.classList.add('paused');
      addLog('info', '⏸️ Execution paused - explain to judges');
    } else {
      btn.innerHTML = '⏸️ Pause (for explanation)';
      btn.classList.remove('paused');
      addLog('info', '▶️ Execution resumed');
    }
  }
}

// Execute the solution on the actual track visualization
let executionInterval = null;

function startTrackExecution(solution, trainAffected, otherTrain) {
  // Clear any existing intervals - IMPORTANT: Stop the main update loop!
  if (executionInterval) clearInterval(executionInterval);
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
  
  // Set execution mode - prevents main loop from interfering
  state.isExecuting = true;
  state.isRunning = true;
  
  // Get initial train positions for main conflict trains (handle both RAJ and RAJ_JPR formats)
  let raj = state.trains.find(t => t.id?.includes('RAJ'));
  let frt = state.trains.find(t => t.id?.includes('FRT'));
  
  if (!raj || !frt) {
    console.error('Trains not found for execution. Available trains:', state.trains.map(t => t.id));
    addLog('error', 'Trains not found for execution');
    return;
  }
  
  console.log('Starting execution with trains:', raj.id, frt.id);
  
  // Key positions for West Line - DSB (Sadar Bazar) at 4km is the holding loop
  const DSB_KM = 4;              // DSB Holding Loop position
  const RAJ_STOP_POSITION = 4;   // RAJ stops at DSB (4 km from NDLS)
  const FRT_STOP_POSITION = 4;   // FRT stops at DSB (4 km from NDLS)
  
  // Get actual starting positions from trains
  let rajSpeed = raj.speed || 50;
  let frtSpeed = frt.speed || 15;
  let rajPos = raj.position;
  let frtPos = frt.position;
  let rajStopped = false;
  let frtStopped = false;
  let trainSlowed = false;
  
  // DETERMINE WHICH TRAIN TO STOP/SLOW BASED ON SOLUTION
  const solutionType = solution.type || 'stop';
  const stopTrain = solution.train_affected || trainAffected || 'FRT';
  const passingTrain = solution.train_passing || otherTrain || 'RAJ';
  
  // Log the solution being executed - DETAILED DEBUG
  console.log('=== SOLUTION EXECUTION DEBUG ===');
  console.log('Full solution object:', JSON.stringify(solution, null, 2));
  console.log(`Solution type: "${solutionType}"`);
  console.log(`Train affected (stopTrain): "${stopTrain}"`);
  console.log(`Train passing: "${passingTrain}"`);
  console.log('================================');
  
  addLog('info', `📍 Executing: ${solution.action}`);
  addLog('info', `📍 Type: ${solutionType}, Affected: ${stopTrain}`);
  addLog('info', `📍 RAJ at ${rajPos.toFixed(1)} km, FRT at ${frtPos.toFixed(1)} km`);
  
  // Determine stop position based on which train stops (DSB at 4km for both)
  const stopPosition = DSB_KM;
  
  // Phase tracking
  let phase = 1;
  
  // Update display - DYNAMIC based on which train stops
  const updateDisplay = () => {
    const rajSpeedEl = document.getElementById('rajSpeedDisplay');
    const frtSpeedEl = document.getElementById('frtSpeedDisplay');
    const rajPosEl = document.getElementById('rajPosDisplay');
    const frtPosEl = document.getElementById('frtPosDisplay');
    const platformEl = document.getElementById('platformDisplay');
    
    if (rajSpeedEl) rajSpeedEl.textContent = rajStopped ? 'STOPPED' : `${rajSpeed.toFixed(0)} km/h`;
    if (frtSpeedEl) frtSpeedEl.textContent = frtStopped ? 'STOPPED' : `${frtSpeed.toFixed(0)} km/h`;
    if (rajPosEl) rajPosEl.textContent = `${rajPos.toFixed(1)} km`;
    if (frtPosEl) frtPosEl.textContent = `${frtPos.toFixed(1)} km`;
    // Show platform based on which train stopped
    if (platformEl) {
      if (rajStopped) platformEl.textContent = 'Platform 2';
      else if (frtStopped) platformEl.textContent = 'Platform 9';
    }
  };
  
  // Mark step complete
  const completeStep = (stepNum) => {
    const stepEl = document.getElementById(`step${stepNum}`);
    if (stepEl) {
      stepEl.classList.remove('active');
      stepEl.classList.add('complete');
      stepEl.querySelector('.step-status').textContent = '✓';
    }
    const progress = document.getElementById('executionProgress');
    if (progress) {
      progress.style.width = `${stepNum * 25}%`;
    }
  };
  
  // Activate next step
  const activateStep = (stepNum) => {
    const stepEl = document.getElementById(`step${stepNum}`);
    if (stepEl) {
      stepEl.classList.add('active');
    }
  };
  
  // Update ALL train visuals on track (including background trains) - SMOOTH
  const updateTrackVisuals = () => {
    // Update main conflict trains in state (handle both RAJ and RAJ_JPR formats)
    const rajTrain = state.trains.find(t => t.id?.includes('RAJ'));
    const frtTrain = state.trains.find(t => t.id?.includes('FRT'));
    
    if (rajTrain) {
      rajTrain.position = rajPos;
      rajTrain.speed = rajSpeed;
      rajTrain.state = rajStopped ? 'stopped' : 'moving';
    }
    if (frtTrain) {
      frtTrain.position = frtPos;
      frtTrain.speed = frtSpeed;
      frtTrain.state = frtStopped ? 'stopped' : 'moving';
    }
    
    // Move background trains (LOC1, LOC2, etc.) - they should keep running
    moveBackgroundTrains();
    
    // Re-render trains on Delhi SVG for smooth visual update
    if (USE_EXTERNAL_SVG) {
      renderTrainsOnDelhiSVG();
    } else {
      // Legacy: Use smooth CSS transitions for train movement
      state.trains.forEach(train => {
        const marker = document.getElementById(`train-${train.id}`);
        if (marker) {
          const pos = getTrainPosition(train);
          marker.style.transition = 'transform 0.15s linear';
          marker.setAttribute('transform', `translate(${pos.x}, ${pos.y})`);
        }
      });
    }
    
    // Update train cards in bottom panel
    updateTrainList();
    
    // Update train details panel if one is selected
    updateTrainDetailsPanel();
  };
  
  // DYNAMIC EXECUTION based on solution type
  // Execution loop - runs every 150ms for SMOOTH animation
  executionInterval = setInterval(() => {
    // Respect both local execution pause AND global scenario pause
    if (state.executionPaused || !state.isRunning) return;
    
    // Movement speeds per tick
    const MOVE_PER_TICK = 0.4;
    const SLOW_MOVE = 0.2;
    
    // DIFFERENT BEHAVIOR BASED ON SOLUTION TYPE
    // Log which execution path is taken (only on first tick)
    if (phase === 1 && !state._executionLogged) {
      state._executionLogged = true;
      console.log(`Execution path: type="${solutionType}", stopTrain="${stopTrain}"`);
    }
    
    if (solutionType === 'stop' && (stopTrain === 'FRT' || stopTrain?.includes('FRT'))) {
      // === SOLUTION: STOP FRT, RAJ PASSES ===
      executeStopFRT();
    } else if (solutionType === 'stop' && (stopTrain === 'RAJ' || stopTrain?.includes('RAJ'))) {
      // === SOLUTION: STOP RAJ, FRT PASSES ===
      executeStopRAJ();
    } else if (solutionType === 'slow') {
      // === SOLUTION: SLOW ONE TRAIN ===
      executeSlowTrain();
    } else if (solutionType === 'both_slow') {
      // === SOLUTION: BOTH TRAINS SLOW ===
      executeBothSlow();
    } else if (solutionType === 'slow_and_stop') {
      // === SOLUTION: SLOW ONE + STOP OTHER ===
      executeSlowAndStop();
    } else {
      // Default: Stop FRT (most common)
      console.log('Using DEFAULT execution (stopFRT)');
      executeStopFRT();
    }
    
    // Clamp positions to valid range
    rajPos = Math.max(0, Math.min(rajPos, ROUTE.totalKm));
    frtPos = Math.max(0, Math.min(frtPos, ROUTE.totalKm));
    
    updateDisplay();
    updateTrackVisuals();
    
    // Update AI Performance in real-time (every 5 ticks = ~750ms)
    if (!state._aiUpdateCounter) state._aiUpdateCounter = 0;
    state._aiUpdateCounter++;
    if (state._aiUpdateCounter % 5 === 0) {
      updateAIPerformanceRealTime('executing', solution);
    }
    
    // === EXECUTION FUNCTIONS FOR EACH SOLUTION TYPE ===
    
    function executeStopFRT() {
      // WEST LINE: FRT stops at DSB (4km), RAJ passes
      // FRT starts at 0km going forward (toward DSB), RAJ starts at 15km going backward (toward NDLS)
      switch (phase) {
        case 1:  // FRT approaches DSB, RAJ continues toward NDLS
          // FRT moving forward (0 → 4km)
          if (frtPos < DSB_KM - 0.5) {
            frtPos += MOVE_PER_TICK * 0.8;
            frtSpeed = Math.max(10, frtSpeed);
          } else {
            trainSlowed = true;
          }
          // RAJ moving backward (15 → 0km)
          rajPos -= SLOW_MOVE * 0.8;
          
          if (trainSlowed || frtPos >= DSB_KM - 0.5) {
            completeStep(1); activateStep(2); phase = 2;
            addLog('info', `🚂 FRT approaching DSB Holding Loop`);
          }
          break;
        case 2:  // FRT stops at DSB
          // RAJ continues toward NDLS
          rajPos -= SLOW_MOVE * 0.6;
          
          if (!frtStopped) {
            if (frtPos < DSB_KM) {
              frtPos += MOVE_PER_TICK * 0.3;
            }
            if (frtPos >= DSB_KM - 0.2) {
              frtPos = DSB_KM; frtSpeed = 0; frtStopped = true;
              completeStep(2); activateStep(3); phase = 3;
              addLog('info', `🚂 FRT stopped at DSB Holding Loop (4 km)`);
            }
          }
          break;
        case 3:  // RAJ passes DSB while FRT waits
          frtSpeed = 0;
          rajPos -= MOVE_PER_TICK * 0.8;
          if (rajSpeed < 50) rajSpeed += 2;
          
          // RAJ has passed DSB (position < 4km means past DSB heading to NDLS)
          if (rajPos < DSB_KM - 1) {
            completeStep(3); activateStep(4); phase = 4;
            addLog('success', `🚄 RAJ passed DSB safely → NDLS`);
          }
          break;
        case 4:  // Complete - RAJ continues to NDLS
          rajPos -= MOVE_PER_TICK * 0.5;
          if (rajPos < DSB_KM - 3) finishExecution();
          break;
      }
    }
    
    function executeStopRAJ() {
      // WEST LINE: RAJ stops at DSB (4km), FRT passes
      // RAJ starts at 15km going backward (toward NDLS), FRT starts at 0km going forward
      switch (phase) {
        case 1:  // RAJ approaches DSB, FRT continues
          // RAJ moving backward (15 → 4km)
          if (rajPos > DSB_KM + 0.5) {
            rajPos -= MOVE_PER_TICK * 0.8;
            rajSpeed = Math.max(30, rajSpeed - 2);
          } else {
            trainSlowed = true;
          }
          // FRT moving forward slowly
          frtPos += SLOW_MOVE * 0.5;
          
          if (trainSlowed || rajPos <= DSB_KM + 0.5) {
            completeStep(1); activateStep(2); phase = 2;
            addLog('info', `🚄 RAJ approaching DSB Holding Loop`);
          }
          break;
        case 2:  // RAJ stops at DSB
          // FRT continues forward
          frtPos += SLOW_MOVE * 0.4;
          
          if (!rajStopped) {
            if (rajPos > DSB_KM) {
              rajPos -= MOVE_PER_TICK * 0.3;
            }
            if (rajPos <= DSB_KM + 0.2) {
              rajPos = DSB_KM; rajSpeed = 0; rajStopped = true;
              completeStep(2); activateStep(3); phase = 3;
              addLog('info', `🚄 RAJ stopped at DSB Holding Loop (4 km)`);
            }
          }
          break;
        case 3:  // FRT passes DSB while RAJ waits
          rajSpeed = 0;
          frtPos += MOVE_PER_TICK * 0.6;
          if (frtSpeed < 15) frtSpeed += 1;
          
          // FRT has passed DSB (position > 4km means past DSB heading to DEC)
          if (frtPos > DSB_KM + 1) {
            completeStep(3); activateStep(4); phase = 4;
            addLog('success', `🚂 FRT passed DSB safely → DEC`);
          }
          break;
        case 4:  // Complete - FRT continues to DEC
          frtPos += MOVE_PER_TICK * 0.4;
          if (frtPos > DSB_KM + 3) finishExecution();
          break;
      }
    }
    
    function executeSlowTrain() {
      // WEST LINE: One train slows by 25% - TEMPORARY SOLUTION
      // RAJ going backward (15→0), FRT going forward (0→15)
      const slowingTrain = stopTrain;
      const isSlowingRaj = slowingTrain?.includes('RAJ');
      switch (phase) {
        case 1:  // Slowing train reduces speed
          if (isSlowingRaj) {
            if (rajSpeed > 35) rajSpeed -= 2;
            else trainSlowed = true;
            rajPos -= SLOW_MOVE * 0.5;  // RAJ going backward (decreasing)
            frtPos += MOVE_PER_TICK * 0.6;  // FRT going forward (increasing)
          } else {
            if (frtSpeed > 10) frtSpeed -= 1;
            else trainSlowed = true;
            frtPos += SLOW_MOVE * 0.3;  // FRT going forward (increasing)
            rajPos -= MOVE_PER_TICK * 0.6;  // RAJ going backward (decreasing)
          }
          if (trainSlowed) {
            completeStep(1); activateStep(2); phase = 2;
            addLog('info', `${slowingTrain} slowed to ${isSlowingRaj ? rajSpeed.toFixed(0) : frtSpeed.toFixed(0)} km/h`);
          }
          break;
        case 2:  // Continue at reduced speeds - trains getting closer
          rajPos -= SLOW_MOVE * 0.5;  // RAJ backward
          frtPos += SLOW_MOVE * 0.4;  // FRT forward
          if (Math.abs(rajPos - frtPos) < 3) {
            completeStep(2); activateStep(3); phase = 3;
            addLog('warning', `⚠️ Trains still approaching - temporary solution!`);
          }
          break;
        case 3:  // Show warning - conflict not resolved
          rajPos -= SLOW_MOVE * 0.3;
          frtPos += SLOW_MOVE * 0.3;
          if (Math.abs(rajPos - frtPos) < 2) {
            completeStep(3); activateStep(4); phase = 4;
            addLog('danger', `🚨 CONFLICT NOT RESOLVED - Slowing only delays collision!`);
          }
          break;
        case 4:
          finishTemporarySolution('slow');
          break;
      }
    }
    
    function executeBothSlow() {
      // WEST LINE: Both trains slow by 15% - TEMPORARY SOLUTION
      switch (phase) {
        case 1:
          if (rajSpeed > 40) rajSpeed -= 2;
          if (frtSpeed > 12) frtSpeed -= 1;
          if (rajSpeed <= 40 && frtSpeed <= 12) trainSlowed = true;
          rajPos -= SLOW_MOVE * 0.4;  // RAJ backward
          frtPos += SLOW_MOVE * 0.3;  // FRT forward
          if (trainSlowed) {
            completeStep(1); activateStep(2); phase = 2;
            addLog('info', `Both trains slowed: RAJ ${rajSpeed.toFixed(0)}km/h, FRT ${frtSpeed.toFixed(0)}km/h`);
          }
          break;
        case 2:
          rajPos -= SLOW_MOVE * 0.4;
          frtPos += SLOW_MOVE * 0.3;
          if (Math.abs(rajPos - frtPos) < 3) {
            completeStep(2); activateStep(3); phase = 3;
            addLog('warning', `⚠️ Trains still approaching - temporary solution!`);
          }
          break;
        case 3:
          rajPos -= SLOW_MOVE * 0.3;
          frtPos += SLOW_MOVE * 0.2;
          if (Math.abs(rajPos - frtPos) < 2) {
            completeStep(3); activateStep(4); phase = 4;
            addLog('danger', `🚨 CONFLICT NOT RESOLVED - Both slowing only delays collision!`);
          }
          break;
        case 4:
          finishTemporarySolution('both_slow');
          break;
      }
    }
    
    function executeSlowAndStop() {
      // WEST LINE: Slow RAJ + Stop FRT - PERMANENT SOLUTION
      switch (phase) {
        case 1:
          if (rajSpeed > 35) rajSpeed -= 2;
          rajPos -= SLOW_MOVE * 0.5;  // RAJ backward
          if (frtPos < DSB_KM - 0.5) {
            frtPos += MOVE_PER_TICK * 0.6;
          }
          if (rajSpeed <= 35) {
            completeStep(1); activateStep(2); phase = 2;
            addLog('info', `RAJ slowed to ${rajSpeed.toFixed(0)} km/h, FRT approaching DSB`);
          }
          break;
        case 2:
          rajPos -= SLOW_MOVE * 0.4;
          if (!frtStopped && frtPos < DSB_KM) {
            frtPos += MOVE_PER_TICK * 0.3;
            if (frtPos >= DSB_KM - 0.2) {
              frtPos = DSB_KM; frtSpeed = 0; frtStopped = true;
              completeStep(2); activateStep(3); phase = 3;
              addLog('info', `FRT stopped at DSB Holding Loop, RAJ continuing`);
            }
          }
          break;
        case 3:
          if (rajSpeed < 45) rajSpeed += 1;
          rajPos -= MOVE_PER_TICK * 0.6;  // RAJ continues backward toward NDLS
          // RAJ has passed DSB (position < 4km)
          if (rajPos < DSB_KM - 1) {
            completeStep(3); activateStep(4); phase = 4;
            addLog('success', `RAJ passed DSB safely → NDLS`);
          }
          break;
        case 4:
          rajPos -= MOVE_PER_TICK * 0.4;
          if (rajPos < DSB_KM - 3) finishExecution();  // PERMANENT - scenario complete
          break;
      }
    }
    
    // PERMANENT SOLUTION - Scenario complete
    function finishExecution() {
      clearInterval(executionInterval);
      executionInterval = null;
      state._executionLogged = false;
      state.isExecuting = false;
      state.isRunning = false;
      completeStep(4);
      
      // Update AI Performance - COMPLETED
      updateAIPerformanceRealTime('completed', solution);
      
      // Show sustainability impact with before/after comparison
      showSustainabilityImpact(solution);
      
      // Show completion message in AI panel
      setTimeout(() => showScenarioComplete(solution), 500);
      addLog('success', `🎉 Scenario Complete - Collision Avoided!`);
    }
    
    // TEMPORARY SOLUTION - Show conflict again
    function finishTemporarySolution(solutionName) {
      clearInterval(executionInterval);
      executionInterval = null;
      state._executionLogged = false;
      completeStep(4);
      
      // Update AI Performance - TEMPORARY (not resolved)
      updateAIPerformanceRealTime('temporary', solution);
      
      // Show warning that this was temporary
      addLog('danger', `⚠️ TEMPORARY SOLUTION: "${solutionName}" only delayed the conflict!`);
      addLog('warning', `🔄 Conflict will reappear - choose a PERMANENT solution (Stop FRT or Stop RAJ)`);
      
      // After a short delay, show the conflict modal again
      setTimeout(() => {
        showTemporarySolutionWarning(solution);
      }, 500);
    }
    
  }, 150);  // 150ms for smooth animation
}

// Show warning that temporary solution didn't resolve conflict
function showTemporarySolutionWarning(solution) {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // Clear execution state
  state.executingSolution = null;
  state.executionPhase = 0;
  state.isExecuting = false;
  
  aiStatusBadge.textContent = '⚠️ CONFLICT PERSISTS';
  aiStatusBadge.style.background = 'rgba(255, 107, 53, 0.3)';
  aiStatusBadge.style.color = '#ff6b35';
  
  aiContent.innerHTML = `
    <div class="temporary-warning">
      <div class="warning-icon">⚠️</div>
      <div class="warning-title">TEMPORARY SOLUTION - CONFLICT NOT RESOLVED!</div>
      <div class="warning-subtitle">"${solution.action}" only delayed the collision</div>
      
      <div class="warning-explanation">
        <p><strong>Why this didn't work:</strong></p>
        <ul>
          <li>Slowing trains only buys time, doesn't resolve head-on conflict</li>
          <li>Both trains are still on the same track (Track 1)</li>
          <li>Track 2 is blocked by MEMO - no switching possible</li>
          <li>Collision will still occur unless one train STOPS</li>
        </ul>
      </div>
      
      <div class="warning-recommendation">
        <p><strong>✅ Recommended PERMANENT solutions:</strong></p>
        <ul>
          <li><strong>Stop FRT at Platform 9</strong> - Best choice (respects priority)</li>
          <li><strong>Stop RAJ at Platform 2</strong> - Works but priority violation</li>
        </ul>
      </div>
      
      <div class="warning-actions">
        <button type="button" class="btn btn-primary" onclick="showConflictAgain()">
          🔄 Show Solutions Again
        </button>
        <button type="button" class="btn btn-secondary" onclick="resetScenario()">
          ↩️ Reset Scenario
        </button>
      </div>
    </div>
  `;
  
  addLog('warning', `⚠️ Temporary solution failed - choose a permanent solution!`);
}

// Show scenario complete message
function showScenarioComplete(solution) {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // Update status badge
  aiStatusBadge.textContent = '✅ RESOLVED';
  aiStatusBadge.style.background = 'rgba(0, 255, 136, 0.2)';
  aiStatusBadge.style.color = '#00ff88';
  
  // Get train info
  const trainAffected = solution.train_affected || 'FRT';
  const trainPassing = trainAffected?.includes('FRT') ? 'RAJ' : 'FRT';
  
  aiContent.innerHTML = `
    <div class="scenario-complete">
      <div class="complete-header">
        <span class="complete-icon">🎉</span>
        <span class="complete-title">SCENARIO COMPLETE</span>
      </div>
      
      <div class="complete-message">
        <div class="success-badge">✅ COLLISION AVOIDED</div>
        <div class="success-text">AI recommendation was successfully executed</div>
      </div>
      
      <div class="complete-summary">
        <div class="summary-title">📊 RESOLUTION SUMMARY</div>
        <div class="summary-row">
          <span class="summary-label">Solution Applied:</span>
          <span class="summary-value">${solution.action || 'Stop ' + trainAffected}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Train Stopped:</span>
          <span class="summary-value">${trainAffected} at DSB Holding Loop</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Train Passed:</span>
          <span class="summary-value">${trainPassing} → Destination</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Energy Used:</span>
          <span class="summary-value">${solution.energy_kwh?.toFixed(0) || '--'} kWh</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Delay Added:</span>
          <span class="summary-value">+${solution.delay_minutes?.toFixed(0) || '8'} min to ${trainAffected}</span>
        </div>
      </div>
      
      <div class="complete-lesson">
        <div class="lesson-title">💡 KEY TAKEAWAY</div>
        <div class="lesson-text">
          The AI used <strong>60% Priority + 40% Energy</strong> formula to decide.
          ${trainAffected?.includes('FRT') 
            ? 'Freight (P6) was stopped because Rajdhani (P2) has higher priority - ensuring passenger trains are not delayed.'
            : 'Rajdhani (P2) was stopped - this is a priority violation but may be justified by energy savings.'}
        </div>
      </div>
      
      <div class="complete-actions">
        <button type="button" class="btn btn-primary" onclick="resetScenario()">
          🔄 Try Another Scenario
        </button>
      </div>
    </div>
  `;
}

// Reset scenario - reload the current scenario from scratch
function resetScenario() {
  // Clear any intervals first
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
  if (executionInterval) {
    clearInterval(executionInterval);
    executionInterval = null;
  }
  if (collisionCheckInterval) {
    clearInterval(collisionCheckInterval);
    collisionCheckInterval = null;
  }
  
  // Reset state
  state.conflict = null;
  state.solutions = [];
  state.isRunning = false;
  state.isExecuting = false;
  state.conflictAlreadyHandled = false;
  state.pendingCollision = null;
  state.executingSolution = null;
  state.executionPhase = 0;
  state._executionLogged = false;
  
  // Reset AI panel
  resetAIPanel();
  
  // If we have a current scenario, reload it
  if (state.currentScenario) {
    const scenarioId = state.currentScenario.id || 1;
    addLog('info', `🔄 Resetting scenario ${scenarioId}...`);
    loadScenario(scenarioId);
  } else {
    addLog('info', '🔄 Scenario reset. Select a scenario to start.');
  }
}

// Show conflict modal again after temporary solution
function showConflictAgain() {
  // Reset conflict handled flag so we can show solutions again
  state.conflictAlreadyHandled = false;
  state.isExecuting = false;
  
  // Show the conflict modal with solutions
  if (state.conflict && state.solutions && state.solutions.length > 0) {
    showConflictModal(state.conflict, state.solutions);
    addLog('info', `🔄 Showing solutions again - choose a PERMANENT solution`);
  } else {
    addLog('error', `No conflict data available - reset scenario`);
  }
}

// Move ALL background trains during execution (keeps the scene alive)
function moveBackgroundTrains() {
  // Small movement per tick for smooth animation (matches 150ms interval)
  const MOVE_FACTOR = 0.15;  // km per tick base
  
  // Station names for termination messages (Delhi Section)
  const stationNames = {
    0: 'New Delhi', 5: 'Nizamuddin', 7: 'Old Delhi', 12: 'Anand Vihar',
    25: 'Faridabad', 32: 'Narela', 42: 'Sonipat', 60: 'Palwal', 141: 'Mathura'
  };
  
  const getStationNameLocal = (km) => {
    let closest = 'Unknown';
    let minDist = Infinity;
    for (const [stationKm, name] of Object.entries(stationNames)) {
      const dist = Math.abs(km - parseInt(stationKm));
      if (dist < minDist) {
        minDist = dist;
        closest = name;
      }
    }
    return closest;
  };
  
  state.trains.forEach(train => {
    // Skip main conflict trains (handled separately in execution)
    if (train.id === 'RAJ' || train.id === 'FRT') return;
    
    // Skip already stopped/terminated/arrived trains
    if (train.state === 'stopped' || train.state === 'terminated' || train.state === 'arrived') return;
    
    const speed = train.speed || 60;
    const prevState = train.state;
    
    // Calculate movement based on speed (scaled for smooth animation)
    const moveAmount = MOVE_FACTOR * (speed / 60);  // Scale by speed ratio
    
    // Move based on direction
    if (train.direction === 'forward') {
      train.position += moveAmount;
      // Check if reached destination
      if (train.position >= (train.destination || ROUTE.totalKm)) {
        train.position = train.destination || ROUTE.totalKm;
        train.speed = 0;
        train.state = 'arrived';
        const stationName = getStationNameLocal(train.position);
        // Assign a platform (odd for local trains)
        train.platform = train.id.includes('LOC') ? 7 : 2;
        if (prevState !== 'arrived') {
          addLog('info', `🚃 ${train.id} arrived at ${stationName} Platform ${train.platform}`);
        }
      }
    } else if (train.direction === 'backward') {
      train.position -= moveAmount;
      // Check if reached destination (New Delhi Hub = 0)
      if (train.position <= (train.destination || 0)) {
        train.position = Math.max(0, train.destination || 0);
        train.speed = 0;
        train.state = 'terminated';
        const stationName = getStationNameLocal(train.position);
        // Assign a platform (odd for local trains)
        train.platform = train.id.includes('LOC') ? 5 : 1;
        if (prevState !== 'terminated') {
          addLog('info', `🚃 ${train.id} terminated at ${stationName} Platform ${train.platform}`);
        }
      }
    }
    
    // Clamp positions to valid range
    train.position = Math.max(0, Math.min(train.position, ROUTE.totalKm));
  });
}

// Show solution completed successfully
function showSolutionComplete(solution, conflict) {
  const aiContent = document.getElementById('aiContent');
  const aiStatusBadge = document.getElementById('aiStatusBadge');
  
  // STOP everything - scenario is complete
  if (executionInterval) {
    clearInterval(executionInterval);
    executionInterval = null;
  }
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
  
  // Clear execution state
  state.executingSolution = null;
  state.executionPhase = 0;
  state.isExecuting = false;
  state.isRunning = false;  // Stop the simulation completely
  
  aiStatusBadge.textContent = '✓ SCENARIO COMPLETE';
  aiStatusBadge.style.background = 'rgba(0, 255, 136, 0.3)';
  aiStatusBadge.style.color = '#00ff88';
  
  const trainAffected = solution.train_affected || 'FRT';
  
  // Get final train positions (handle both RAJ and RAJ_JPR formats)
  const raj = state.trains.find(t => t.id?.includes('RAJ'));
  const frt = state.trains.find(t => t.id?.includes('FRT'));
  
  aiContent.innerHTML = `
    <div class="solution-complete">
      <div class="complete-icon">🎉</div>
      <div class="complete-title">SCENARIO 1 COMPLETE</div>
      <div class="complete-subtitle">AI Successfully Prevented Head-On Collision</div>
      
      <div class="complete-summary">
        <div class="summary-item highlight">
          <span class="summary-label">Result:</span>
          <span class="summary-value success">COLLISION AVOIDED ✓</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Solution Used:</span>
          <span class="summary-value">${solution.action}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Energy Consumed:</span>
          <span class="summary-value">${solution.energy_kwh?.toFixed(0)} kWh</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Delay Added:</span>
          <span class="summary-value">+${solution.delay_minutes?.toFixed(1)} min to ${trainAffected}</span>
        </div>
      </div>
      
      <div class="final-positions">
        <div class="final-train">
          <span class="train-icon">🚄</span>
          <span class="train-label">RAJ (Rajdhani):</span>
          <span class="train-final-pos">${raj?.position?.toFixed(1) || '--'} km - Passed safely ✓</span>
        </div>
        <div class="final-train stopped">
          <span class="train-icon">🚂</span>
          <span class="train-label">FRT (Freight):</span>
          <span class="train-final-pos">${frt?.position?.toFixed(1) || '--'} km</span>
          <span class="train-platform-badge">Platform 9</span>
        </div>
      </div>
      
      <div class="demo-actions">
        <button type="button" class="btn btn-secondary" onclick="resetScenario()">
          🔄 Reset Scenario
        </button>
        <button type="button" class="btn btn-primary" onclick="showNextScenarioPrompt()">
          Next Scenario →
        </button>
      </div>
    </div>
  `;
  
  // Track AI performance
  state.aiPerformance.conflictsResolved++;
  state.aiPerformance.totalDecisions++;
  updateAIPerformance();
  
  addLog('success', `🎉 Scenario Complete! Collision avoided, ${trainAffected} delayed by ${solution.delay_minutes?.toFixed(1)} min`);
}

// Reset current scenario
function resetScenario() {
  // Clear any running intervals
  if (executionInterval) {
    clearInterval(executionInterval);
    executionInterval = null;
  }
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
  
  // Reset state
  state.conflict = null;
  state.solutions = [];
  state.conflictAlreadyHandled = false;
  state.executingSolution = null;
  state.executionPhase = 0;
  state.isRunning = false;
  state.isExecuting = false;
  
  // Reload the scenario
  if (state.currentScenario) {
    // Extract numeric ID from scenario (could be "s1" or 1)
    let scenarioId = state.currentScenario.id;
    if (typeof scenarioId === 'string') {
      scenarioId = parseInt(scenarioId.replace('s', '')) || 1;
    }
    loadScenario(scenarioId);
    addLog('info', '🔄 Scenario reset to initial state');
  }
}

// Prompt to show next scenario
function showNextScenarioPrompt() {
  const aiContent = document.getElementById('aiContent');
  aiContent.innerHTML = `
    <div class="next-scenario-prompt">
      <div class="prompt-icon">📋</div>
      <div class="prompt-title">Ready for Next Scenario?</div>
      <div class="prompt-text">Select another scenario from the dropdown above to continue the demo.</div>
      <div class="scenario-suggestions">
        <button type="button" class="btn btn-secondary" onclick="loadScenario(2)">Scenario 2: Ghat Section</button>
        <button type="button" class="btn btn-secondary" onclick="loadScenario(3)">Scenario 3: Multi-Train</button>
      </div>
    </div>
  `;
}

// Continue simulation after resolution
function continueAfterResolution() {
  clearConflictZone();
  state.conflict = null;
  state.conflictAlreadyHandled = false;
  
  // Reset AI panel to monitoring state
  resetAIPanel();
  
  // Resume simulation
  state.isRunning = true;
  startSimulation();
  
  addLog('info', '▶️ Simulation resumed - all trains operating normally');
}


// ============================================
// DECISION LOG
// ============================================
function addLog(type, message) {
  const container = document.getElementById('logContent');
  const now = new Date();
  const timeStr = now.toLocaleTimeString('en-IN', { hour12: false });
  
  const entry = document.createElement('div');
  entry.className = `log-entry ${type}`;
  entry.innerHTML = `
    <span class="log-time">${timeStr}</span>
    <span class="log-msg">${message}</span>
  `;
  
  container.appendChild(entry);
  container.scrollTop = container.scrollHeight;
  
  // Keep only last 50 entries
  while (container.children.length > 50) {
    container.removeChild(container.firstChild);
  }
}

function clearLog() {
  const container = document.getElementById('logContent');
  container.innerHTML = `
    <div class="log-entry info">
      <span class="log-time">${new Date().toLocaleTimeString('en-IN', { hour12: false })}</span>
      <span class="log-msg">Log cleared. System ready.</span>
    </div>
  `;
}

// ============================================
// UTILITY FUNCTIONS
// ============================================
function formatTime(seconds) {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}

// ============================================
// PHYSICS CALCULATIONS PANEL
// ============================================
function toggleCalcPanel() {
  const panel = document.getElementById('calcPanel');
  panel.classList.toggle('collapsed');
}

function updateCalculationsPanel(train1, train2, conflict) {
  // Update train 1 (RAJ) calculations
  const mass1 = train1.mass_tons || 850;
  const speed1 = train1.speed || 110;
  const energy1 = train1.energy_to_stop_kwh || calculateStopEnergy(mass1, speed1);
  
  document.getElementById('calcTrain1').querySelector('.calc-train-name').textContent = train1.id || 'RAJ';
  document.getElementById('calcTrain1').querySelector('.calc-formula').innerHTML = 
    `KE = 0.5 × <span class="calc-var">${mass1}t</span> × <span class="calc-var">${speed1}²</span>`;
  document.getElementById('calcEnergy1').textContent = energy1;
  
  // Update train 2 (FRT) calculations
  const mass2 = train2.mass_tons || 4200;
  const speed2 = train2.speed || 50;
  const energy2 = train2.energy_to_stop_kwh || calculateStopEnergy(mass2, speed2);
  
  document.getElementById('calcTrain2').querySelector('.calc-train-name').textContent = train2.id || 'FRT';
  document.getElementById('calcTrain2').querySelector('.calc-formula').innerHTML = 
    `KE = 0.5 × <span class="calc-var">${mass2}t</span> × <span class="calc-var">${speed2}²</span>`;
  document.getElementById('calcEnergy2').textContent = energy2;
  
  // Update collision time calculations
  const pos1 = train1.position || 22;
  const pos2 = train2.position || 1;
  const distance = Math.abs(pos1 - pos2);
  const closingSpeed = speed1 + speed2;
  const timeToCollision = (distance / closingSpeed * 60).toFixed(2);
  
  document.getElementById('calcDistance').textContent = distance.toFixed(1);
  document.getElementById('calcSpeed1').textContent = speed1;
  document.getElementById('calcSpeed2').textContent = speed2;
  document.getElementById('calcClosing').textContent = closingSpeed;
  document.getElementById('calcDistance2').textContent = distance.toFixed(1);
  document.getElementById('calcClosing2').textContent = closingSpeed;
  document.getElementById('calcTime').textContent = timeToCollision;
  
  // Update energy savings
  const savings = Math.abs(energy2 - energy1);
  const homes = Math.round(savings / 30);
  
  document.getElementById('savingsBad').textContent = `${energy2} kWh`;
  document.getElementById('savingsGood').textContent = `${energy1} kWh`;
  document.getElementById('savingsTotal').textContent = `${savings} kWh`;
  document.getElementById('homesCount').textContent = homes;
  
  // Expand the panel when conflict detected
  const panel = document.getElementById('calcPanel');
  panel.classList.remove('collapsed');
}

function calculateStopEnergy(massTons, speedKmh) {
  // Simplified energy calculation
  // KE = 0.5 * m * v^2, converted to kWh with losses
  const massKg = massTons * 1000;
  const speedMps = speedKmh / 3.6;
  const ke = 0.5 * massKg * speedMps * speedMps;
  const keKwh = ke / 3600000;
  
  // Add braking losses and restart energy (roughly 3x for freight, 2x for passenger)
  const multiplier = massTons > 2000 ? 3.5 : 2.0;
  return Math.round(keKwh * multiplier);
}

// ============================================
// KEYBOARD SHORTCUTS
// ============================================
document.addEventListener('keydown', (e) => {
  // ESC to close modals
  if (e.key === 'Escape') {
    document.getElementById('conflictModal').classList.remove('active');
    document.getElementById('simulationModal').classList.remove('active');
  }
  
  // 1, 2, 3 to load scenarios
  if (e.key >= '1' && e.key <= '3' && !e.ctrlKey && !e.altKey) {
    const id = parseInt(e.key);
    if (state.scenarios.find(s => s.id === id)) {
      loadScenario(id);
    }
  }
  
  // + / - for zoom
  if (e.key === '+' || e.key === '=') {
    setZoom(state.zoom + 25);
  }
  if (e.key === '-') {
    setZoom(state.zoom - 25);
  }
});

// ============================================
// ERROR HANDLING
// ============================================
window.addEventListener('error', (e) => {
  console.error('Application error:', e.error);
  addLog('error', 'An error occurred. Check console for details.');
});

// Retry connection periodically if disconnected
setInterval(() => {
  if (!state.connected) {
    checkBackendConnection();
  }
}, 5000);
