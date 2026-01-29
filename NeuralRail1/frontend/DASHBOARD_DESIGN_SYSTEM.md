# NeuralRail Dashboard Design System

A comprehensive guide to the dashboard UI/UX design, CSS styling, and implementation patterns used in the NeuralRail project. This document serves as a reference for replicating the same styling in other projects.

---

## Table of Contents

1. [Color Palette](#color-palette)
2. [Typography](#typography)
3. [CSS Variables](#css-variables)
4. [Layout Structure](#layout-structure)
5. [Component Styling](#component-styling)
6. [Header & Navigation](#header--navigation)
7. [Secondary Navbar](#secondary-navbar)
8. [Main Dashboard Panels](#main-dashboard-panels)
9. [Side Panel Components](#side-panel-components)
10. [Graphs Section](#graphs-section)
11. [Analytics & Spider Chart](#analytics--spider-chart)
12. [Modals](#modals)
13. [Buttons & Interactive Elements](#buttons--interactive-elements)
14. [Animations](#animations)
15. [Responsive Design](#responsive-design)
16. [JavaScript Integration](#javascript-integration)
17. [Backend API Structure](#backend-api-structure)

---

## Color Palette

### Primary Colors (Dark Theme)

| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Background Primary | `#0a1628` | Main app background |
| Background Secondary | `#0f1f35` | Header, secondary sections |
| Background Panel | `#0d1a2d` | Card/panel backgrounds |
| Background Card | `#1a2d47` | Inner cards, inputs |
| Background Hover | `#243a56` | Hover states |
| Border Color | `#2a4060` | Default borders |
| Border Light | `#3a5070` | Lighter borders |

### Text Colors

| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Text Primary | `#e8f0f8` | Main text, headings |
| Text Secondary | `#8aa4c0` | Subheadings, labels |
| Text Muted | `#5a7a9a` | Hints, disabled text |

### Accent Colors

| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Cyan | `#00d4ff` | Primary accent, links, highlights |
| Green | `#00ff88` | Success, AI indicators, positive values |
| Amber | `#ffb800` | Warnings, paused states |
| Red | `#ff3b3b` | Errors, danger, manual indicators |
| Purple | `#a855f7` | West line, special elements |
| Orange | `#ff6b35` | East line, alerts |

---

## Typography

### Font Families

```css
/* Primary font for UI text */
font-family: 'Inter', sans-serif;

/* Monospace font for data, numbers, code */
font-family: 'JetBrains Mono', monospace;
```

### Font Import (Google Fonts)

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

### Font Sizes

| Element | Size | Weight |
|---------|------|--------|
| Logo Title | 16px | 700 |
| Section Headers | 14px | 600 |
| Panel Headers | 12-13px | 600 |
| Body Text | 12px | 400 |
| Labels | 10-11px | 500 |
| Small Text | 9-10px | 400 |

---

## CSS Variables

```css
:root {
  /* Backgrounds */
  --bg-primary: #0a1628;
  --bg-secondary: #0f1f35;
  --bg-panel: #0d1a2d;
  --bg-card: #1a2d47;
  --bg-hover: #243a56;
  
  /* Borders */
  --border-color: #2a4060;
  --border-light: #3a5070;
  
  /* Text */
  --text-primary: #e8f0f8;
  --text-secondary: #8aa4c0;
  --text-muted: #5a7a9a;
  
  /* Accents */
  --accent-cyan: #00d4ff;
  --accent-green: #00ff88;
  --accent-amber: #ffb800;
  --accent-red: #ff3b3b;
  --accent-purple: #a855f7;
  --accent-orange: #ff6b35;
}
```

---

## Layout Structure

### HTML Structure Overview

```html
<div id="app">
  <!-- Header (56px fixed height) -->
  <header class="header">...</header>
  
  <!-- Dropdown Panels (absolute positioned) -->
  <div class="trains-dropdown-panel">...</div>
  
  <!-- Main Content -->
  <main class="main-content-new">
    <!-- Secondary Navbar -->
    <nav class="secondary-navbar">...</nav>
    
    <!-- Live Feed Section (flex row) -->
    <section class="live-feed-section">
      <!-- Map Panel (flex: 7) -->
      <div class="map-panel-fullscreen">...</div>
      
      <!-- Side Panel (flex: 3) -->
      <aside class="side-panel">...</aside>
    </section>
  </main>
  
  <!-- Graphs Section (below fold) -->
  <section class="graphs-section">...</section>
  
  <!-- Modals -->
  <div class="modal-overlay">...</div>
</div>
```

### Base Layout CSS

```css
* { margin: 0; padding: 0; box-sizing: border-box; }

html, body {
  height: 100%;
  overflow-x: hidden;
  font-family: 'Inter', sans-serif;
  background: #050a12;
  color: var(--text-primary);
  scroll-behavior: smooth;
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-primary);
  overflow-y: auto;
}
```

---

## Component Styling

### Panel Base Style

```css
.panel {
  background: var(--bg-panel);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.panel-header h2, .panel-header h3 {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}
```

### Info Badge

```css
.info-badge {
  padding: 3px 10px;
  border-radius: 12px;
  background: var(--bg-card);
  font-size: 10px;
  color: var(--accent-cyan);
}
```

---

## Header & Navigation

### Header Structure

```html
<header class="header">
  <div class="header-left">
    <div class="logo">
      <span class="logo-icon">🚄</span>
      <div class="logo-text">
        <span class="logo-title">NEURALRAIL</span>
        <span class="logo-subtitle">Delhi Section Controller</span>
      </div>
    </div>
  </div>
  
  <div class="header-center">
    <!-- Corridor Info -->
    <div class="corridor-info">
      <span class="corridor-label">SECTION</span>
      <span class="corridor-name">DELHI HUB (4 Routes)</span>
    </div>
    
    <!-- Scenario Selector -->
    <div class="scenario-nav">
      <select class="scenario-select-nav">...</select>
      <button class="run-btn-nav">▶</button>
    </div>
    
    <!-- Status Indicator -->
    <div class="status-indicator" id="connectionStatus">
      <span class="status-dot"></span>
      <span class="status-text">LIVE</span>
    </div>
  </div>
  
  <div class="header-right">
    <!-- Playback Controls -->
    <div class="playback-nav">
      <button class="playback-btn-nav">⏸️</button>
      <div class="speed-btns-nav">
        <button class="speed-btn-nav" data-speed="0.5">0.5x</button>
        <button class="speed-btn-nav active" data-speed="1">1x</button>
        <button class="speed-btn-nav" data-speed="2">2x</button>
        <button class="speed-btn-nav" data-speed="5">5x</button>
      </div>
    </div>
    
    <!-- Train Dropdown -->
    <button class="trains-dropdown-btn">
      🚂 TRAINS <span class="train-count">0</span>
      <span class="dropdown-arrow">▼</span>
    </button>
    
    <!-- Clock -->
    <div class="clock">
      <span class="clock-time">--:--:--</span>
      <span class="clock-date">-- --- ----</span>
    </div>
  </div>
</header>
```

### Header CSS

```css
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  height: 56px;
  flex-shrink: 0;
}

/* Logo */
.logo { display: flex; align-items: center; gap: 10px; }
.logo-icon { font-size: 24px; }
.logo-title { 
  font-size: 16px; 
  font-weight: 700; 
  letter-spacing: 2px; 
  color: var(--accent-cyan); 
}
.logo-subtitle { 
  font-size: 9px; 
  color: var(--text-muted); 
  letter-spacing: 1px; 
}

/* Status Indicator */
.status-indicator {
  display: flex; 
  align-items: center; 
  gap: 6px;
  padding: 4px 12px; 
  border-radius: 16px;
  background: var(--bg-card); 
  border: 1px solid var(--border-color);
}

.status-indicator.live { 
  border-color: var(--accent-green); 
  background: rgba(0,255,136,0.1); 
}

.status-indicator.live .status-dot { 
  background: var(--accent-green); 
  box-shadow: 0 0 8px var(--accent-green); 
}

.status-dot { 
  width: 6px; 
  height: 6px; 
  border-radius: 50%; 
  background: var(--text-muted); 
  animation: pulse 2s infinite; 
}

@keyframes pulse { 
  0%, 100% { opacity: 1; } 
  50% { opacity: 0.5; } 
}

/* Speed Buttons */
.speed-btns-nav { display: flex; gap: 4px; }
.speed-btn-nav {
  padding: 4px 8px; 
  border-radius: 4px;
  background: var(--bg-card); 
  border: 1px solid var(--border-color);
  color: var(--text-secondary); 
  font-size: 10px; 
  cursor: pointer;
}
.speed-btn-nav.active { 
  background: var(--accent-cyan); 
  color: #000; 
  border-color: var(--accent-cyan); 
}

/* Clock */
.clock { display: flex; flex-direction: column; align-items: flex-end; }
.clock-time { 
  font-size: 14px; 
  font-weight: 600; 
  font-family: 'JetBrains Mono', monospace; 
}
.clock-date { font-size: 9px; color: var(--text-muted); }
```

---

## Secondary Navbar

### Structure

```html
<nav class="secondary-navbar">
  <button class="nav-btn primary" id="viewGraphsBtn">
    <span class="nav-btn-icon">📊</span>
    <span class="nav-btn-text">Graphs</span>
  </button>
  <!-- Add more buttons as needed -->
</nav>
```

### CSS

```css
.secondary-navbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-btn:hover {
  background: var(--bg-hover);
  border-color: var(--accent-cyan);
}

.nav-btn.primary {
  background: linear-gradient(135deg, #0891b2, var(--accent-cyan));
  border: none;
  color: #fff;
  box-shadow: 0 2px 10px rgba(0, 212, 255, 0.25);
}

.nav-btn.primary:hover {
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.4);
  transform: translateY(-1px);
}

.nav-btn-icon { font-size: 16px; }
```

---

## Main Dashboard Panels

### Live Feed Section Layout

```css
.main-content-new {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
  overflow: visible;
}

.live-feed-section {
  flex: 1;
  display: flex;
  gap: 8px;
  min-height: 0;
}
```

### Map Panel (SVG Container)

```html
<div class="map-panel-fullscreen">
  <div class="map-header">
    <div class="map-title">
      <h2>🛤️ LIVE TRACK VIEW - DELHI JUNCTION</h2>
    </div>
    <div class="section-focus-control">
      <label>Focus:</label>
      <select class="section-select">
        <option value="all">All Routes (Overview)</option>
        <!-- More options -->
      </select>
    </div>
    <div class="track-legend-inline">
      <div class="legend-item">
        <span class="legend-color" style="background: #00ff88;"></span>
        <span>North</span>
      </div>
      <!-- More legend items -->
    </div>
  </div>
  <div class="map-container" id="trackContainer">
    <!-- SVG content loaded here -->
  </div>
</div>
```

```css
.map-panel-fullscreen {
  flex: 7;
  display: flex;
  flex-direction: column;
  background: var(--bg-panel);
  border-radius: 8px;
  overflow: hidden;
  min-width: 0;
  max-height: calc(100vh - 160px);
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.map-title h2 {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.section-select {
  padding: 5px 10px;
  border-radius: 6px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  font-size: 10px;
  cursor: pointer;
  min-width: 160px;
}

.track-legend-inline {
  display: flex;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  color: var(--text-secondary);
}

.legend-color {
  width: 12px;
  height: 3px;
  border-radius: 2px;
}

.map-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #050a12;
  overflow: hidden;
  padding: 4px;
  max-height: 100%;
}

.delhi-svg-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.delhi-svg-wrapper svg {
  max-width: 100%;
  max-height: 100%;
  display: block;
}
```

---

## Side Panel Components

### Side Panel Layout

```css
.side-panel {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 280px;
  max-width: 350px;
}
```

### AI Recommendations Panel

```html
<div class="ai-panel">
  <div class="panel-header">
    <h2>🤖 AI RECOMMENDATIONS</h2>
    <span class="info-badge" id="aiStatusBadge">Monitoring</span>
  </div>
  <div class="ai-content" id="aiContent">
    <div class="ai-idle-state">
      <div class="ai-icon">🧠</div>
      <div class="ai-message">AI is monitoring train movements</div>
      <div class="ai-submessage">Recommendations appear when conflicts detected</div>
    </div>
  </div>
</div>
```

```css
.ai-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-panel);
  border-radius: 8px;
  overflow: hidden;
}

.ai-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.ai-idle-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.ai-idle-state .ai-icon {
  font-size: 40px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.ai-idle-state .ai-message {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.ai-idle-state .ai-submessage {
  font-size: 11px;
  color: var(--text-muted);
}
```

### Log Panel

```html
<div class="log-panel">
  <div class="panel-header">
    <h2>📋 LOG</h2>
    <button class="clear-btn" id="clearLog">Clear</button>
  </div>
  <div class="log-content" id="logContent">
    <div class="log-entry info">
      <span class="log-time">--:--:--</span>
      <span class="log-msg">System ready</span>
    </div>
  </div>
</div>
```

```css
.log-panel {
  height: 200px;
  display: flex;
  flex-direction: column;
  background: var(--bg-panel);
  border-radius: 8px;
  overflow: hidden;
}

.clear-btn {
  padding: 3px 10px;
  border-radius: 4px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 10px;
  cursor: pointer;
}

.clear-btn:hover {
  background: var(--accent-red);
  color: #fff;
  border-color: var(--accent-red);
}

.log-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 10px;
}

.log-entry {
  display: flex;
  gap: 8px;
  padding: 4px 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  background: var(--bg-card);
}

.log-entry.info { border-left: 2px solid var(--accent-cyan); }
.log-entry.success { border-left: 2px solid var(--accent-green); }
.log-entry.danger { border-left: 2px solid var(--accent-red); }
.log-entry.warning { border-left: 2px solid var(--accent-amber); }

.log-time { color: var(--text-muted); }
.log-msg { color: var(--text-primary); }
```

---

## Graphs Section

### Combined Graphs Layout

```html
<section class="graphs-section" id="graphsSection">
  <div class="graphs-header">
    <h2>📊 ANALYTICS & GRAPHS</h2>
    <button class="back-to-top-btn" id="backToTopBtn">
      <span>↑</span> Back to Live Feed
    </button>
  </div>
  
  <div class="graphs-container">
    <!-- Time-Space Diagram -->
    <div class="graph-panel">
      <div class="graph-panel-header">
        <h3>🛤️ Time-Space Diagram</h3>
        <span class="info-badge">Distance vs Time</span>
      </div>
      <div class="graph-panel-content">
        <canvas id="graphCanvas"></canvas>
      </div>
    </div>
    
    <!-- Energy Comparison -->
    <div class="graph-panel">
      <div class="graph-panel-header">
        <h3>⚡ Energy Comparison</h3>
        <span class="info-badge">Manual vs AI</span>
      </div>
      <div class="graph-panel-content">
        <div class="energy-summary-compact">
          <div class="energy-stat manual">
            <span class="stat-label">Manual</span>
            <span class="stat-value" id="totalManualEnergy">0 kWh</span>
          </div>
          <div class="energy-stat ai">
            <span class="stat-label">AI</span>
            <span class="stat-value" id="totalAIEnergy">0 kWh</span>
          </div>
          <div class="energy-stat savings">
            <span class="stat-label">Saved</span>
            <span class="stat-value" id="totalEnergySaved">0 kWh</span>
          </div>
        </div>
        <div class="energy-chart-wrapper">
          <canvas id="energyChart"></canvas>
        </div>
      </div>
    </div>
  </div>
</section>
```

### Graphs Section CSS

```css
.graphs-section {
  background: var(--bg-primary);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.graphs-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 16px;
  background: var(--bg-panel);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.graphs-header h2 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.back-to-top-btn {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--accent-cyan), #0891b2);
  border: none;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 10px rgba(0, 212, 255, 0.3);
}

.back-to-top-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.4);
}

.graphs-container {
  display: flex;
  gap: 12px;
}

.graph-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-panel);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.graph-panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.graph-panel-header h3 {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.graph-panel-content {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 350px;
}

.graph-panel-content canvas {
  flex: 1;
  width: 100%;
  height: 100%;
}
```

### Energy Stats Compact

```css
.energy-summary-compact {
  display: flex;
  gap: 8px;
}

.energy-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 10px;
  background: var(--bg-card);
  border-radius: 6px;
}

.energy-stat.manual { border-top: 3px solid #ff6b6b; }
.energy-stat.ai { border-top: 3px solid #00ff88; }
.energy-stat.savings { border-top: 3px solid var(--accent-amber); }

.energy-stat .stat-label {
  font-size: 9px;
  color: var(--text-muted);
  text-transform: uppercase;
}

.energy-stat .stat-value {
  font-size: 14px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-primary);
}

.energy-chart-wrapper {
  flex: 1;
  min-height: 250px;
}

.energy-chart-wrapper canvas {
  width: 100% !important;
  height: 100% !important;
}
```

---

## Analytics & Spider Chart

### Analytics Section Structure

```html
<div class="analytics-section">
  <div class="analytics-header">
    <h3>📈 Performance Analytics</h3>
    <span class="info-badge">AI vs Manual Comparison</span>
  </div>
  <div class="analytics-container">
    <!-- Spider Chart -->
    <div class="spider-chart-panel">
      <canvas id="spiderChart"></canvas>
    </div>
    
    <!-- Analytics Stats -->
    <div class="analytics-stats">
      <div class="analytics-stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-info">
          <span class="stat-title">Conflict Resolution</span>
          <span class="stat-number">95%</span>
          <span class="stat-desc">AI accuracy in resolving conflicts</span>
        </div>
      </div>
      <!-- More stat cards -->
    </div>
  </div>
</div>
```

### Analytics CSS

```css
.analytics-section {
  background: var(--bg-panel);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  margin-top: 12px;
}

.analytics-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.analytics-header h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.analytics-container {
  display: flex;
  gap: 16px;
  padding: 16px;
}

.spider-chart-panel {
  flex: 1;
  min-width: 350px;
  height: 320px;
  background: var(--bg-card);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.analytics-stats {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.analytics-stat-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: all 0.2s;
}

.analytics-stat-card:hover {
  border-color: var(--accent-cyan);
  transform: translateY(-2px);
}

.analytics-stat-card .stat-icon {
  font-size: 28px;
  line-height: 1;
}

.analytics-stat-card .stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.analytics-stat-card .stat-title {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.analytics-stat-card .stat-number {
  font-size: 24px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  color: var(--accent-cyan);
}

.analytics-stat-card .stat-desc {
  font-size: 10px;
  color: var(--text-muted);
}
```

---

## Modals

### Modal Structure

```html
<div class="modal-overlay" id="conflictModal">
  <div class="modal-content">
    <div class="modal-header danger">
      <span class="modal-icon">⚠️</span>
      <h2>CONFLICT DETECTED</h2>
    </div>
    <div class="modal-body">
      <div class="conflict-details">
        <div class="conflict-row">
          <span class="conflict-label">Location</span>
          <span class="conflict-value">Station Name</span>
        </div>
        <!-- More rows -->
      </div>
      <div class="solutions-section">
        <h3>AI RECOMMENDATIONS</h3>
        <div class="solutions-list">
          <div class="solution-card recommended">
            <!-- Solution content -->
          </div>
        </div>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-secondary">Cancel</button>
      <button class="btn btn-primary">Approve</button>
    </div>
  </div>
</div>
```

### Modal CSS

```css
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.8);
  display: none;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-overlay.active { display: flex; }

.modal-content {
  background: var(--bg-panel);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header.danger { background: rgba(255,59,59,0.15); }
.modal-header.info { background: rgba(0,212,255,0.1); }

.modal-header h2 { font-size: 16px; }
.modal-icon { font-size: 24px; }

.modal-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

/* Conflict Details */
.conflict-details {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.conflict-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}

.conflict-row:last-child { border-bottom: none; }
.conflict-label { color: var(--text-muted); font-size: 12px; }
.conflict-value { font-weight: 600; font-size: 13px; }
.conflict-value.danger { color: var(--accent-red); }

/* Solution Cards */
.solutions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.solution-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.solution-card:hover {
  border-color: var(--accent-cyan);
  background: var(--bg-hover);
}

.solution-card.recommended {
  border-color: var(--accent-green);
  background: rgba(0,255,136,0.05);
}
```

---

## Buttons & Interactive Elements

### Button Styles

```css
/* Base Button */
.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

/* Primary Button (Green) */
.btn-primary {
  background: var(--accent-green);
  color: #000;
}
.btn-primary:hover { background: #00e67a; }

/* Secondary Button */
.btn-secondary {
  background: var(--bg-card);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}
.btn-secondary:hover { background: var(--bg-hover); }

/* Run Button (Navbar) */
.run-btn-nav {
  padding: 6px 12px;
  border-radius: 6px;
  background: var(--accent-green);
  border: none;
  color: #000;
  font-weight: 600;
  cursor: pointer;
}
.run-btn-nav:disabled { 
  background: var(--bg-card); 
  color: var(--text-muted); 
  cursor: not-allowed; 
}

/* Dropdown Button */
.trains-dropdown-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.trains-dropdown-btn:hover { 
  background: var(--bg-hover); 
  border-color: var(--accent-cyan); 
}

.trains-dropdown-btn .train-count {
  background: var(--accent-cyan);
  color: #000;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
}
```

### Select/Dropdown Inputs

```css
.scenario-select-nav {
  padding: 6px 12px;
  border-radius: 6px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  font-size: 12px;
  cursor: pointer;
}
```

---

## Animations

### Pulse Animation (Status Indicator)

```css
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-dot {
  animation: pulse 2s infinite;
}
```

### Train Pulse Animation

```css
@keyframes trainPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.5; }
}

.train-marker .train-glow {
  animation: trainPulse 2s ease-in-out infinite;
}
```

### Bounce Arrow Animation

```css
@keyframes bounceArrow {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(5px); }
}

.btn-arrow {
  animation: bounceArrow 1.5s infinite;
}
```

### Hover Transitions

```css
/* Standard hover transition */
.element {
  transition: all 0.2s ease;
}

/* Lift on hover */
.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.4);
}
```

---

## Responsive Design

```css
@media (max-width: 1200px) {
  .side-panel { width: 280px; }
  .track-legend-inline { gap: 10px; }
  .legend-item { font-size: 10px; }
}

@media (max-width: 900px) {
  .live-feed-section { flex-direction: column; }
  .side-panel { 
    width: 100%; 
    flex-direction: row; 
    flex-wrap: wrap;
    height: auto;
  }
  .ai-panel { flex: 1; min-width: 200px; min-height: 150px; }
  .log-panel { flex: 1; min-width: 200px; height: 150px; }
  .map-panel-fullscreen { min-height: 400px; }
  
  .graphs-container { flex-direction: column; }
  .analytics-container { flex-direction: column; }
  .analytics-stats { grid-template-columns: 1fr; }
}
```

---

## JavaScript Integration

### Scroll Navigation

```javascript
// Scroll to section on button click
document.getElementById('viewGraphsBtn').addEventListener('click', () => {
  document.getElementById('graphsSection').scrollIntoView({ behavior: 'smooth' });
});

// Back to top
document.getElementById('backToTopBtn').addEventListener('click', () => {
  document.documentElement.scrollTo({ top: 0, behavior: 'smooth' });
  document.body.scrollTo({ top: 0, behavior: 'smooth' });
  window.scrollTo({ top: 0, behavior: 'smooth' });
});
```

### Clock Update

```javascript
function updateClock() {
  const now = new Date();
  const timeStr = now.toLocaleTimeString('en-IN', { hour12: false });
  const dateStr = now.toLocaleDateString('en-IN', { 
    day: '2-digit', 
    month: 'short', 
    year: 'numeric' 
  });
  
  document.querySelector('.clock-time').textContent = timeStr;
  document.querySelector('.clock-date').textContent = dateStr;
}

setInterval(updateClock, 1000);
```

### Log Entry Function

```javascript
function addLog(type, message) {
  const container = document.getElementById('logContent');
  const time = new Date().toLocaleTimeString('en-IN', { hour12: false });
  
  const entry = document.createElement('div');
  entry.className = `log-entry ${type}`;
  entry.innerHTML = `
    <span class="log-time">${time}</span>
    <span class="log-msg">${message}</span>
  `;
  
  container.insertBefore(entry, container.firstChild);
  
  // Keep only last 50 entries
  while (container.children.length > 50) {
    container.removeChild(container.lastChild);
  }
}

// Usage:
addLog('info', 'System initialized');
addLog('success', 'Connected to backend');
addLog('warning', 'High latency detected');
addLog('danger', 'Connection lost');
```

### Connection Status

```javascript
function setConnectionStatus(connected) {
  const indicator = document.getElementById('connectionStatus');
  
  if (connected) {
    indicator.className = 'status-indicator live';
    indicator.querySelector('.status-text').textContent = 'LIVE';
  } else {
    indicator.className = 'status-indicator error';
    indicator.querySelector('.status-text').textContent = 'OFFLINE';
  }
}
```

### Dropdown Toggle

```javascript
const trainsBtn = document.getElementById('trainsDropdownBtn');
const trainsPanel = document.getElementById('trainsDropdownPanel');

trainsBtn.addEventListener('click', () => {
  trainsPanel.classList.toggle('open');
});

// Close when clicking outside
document.addEventListener('click', (e) => {
  if (!trainsPanel.contains(e.target) && !trainsBtn.contains(e.target)) {
    trainsPanel.classList.remove('open');
  }
});
```

### Speed Button Toggle

```javascript
document.querySelectorAll('.speed-btn-nav').forEach(btn => {
  btn.addEventListener('click', () => {
    // Remove active from all
    document.querySelectorAll('.speed-btn-nav').forEach(b => b.classList.remove('active'));
    // Add active to clicked
    btn.classList.add('active');
    
    const speed = parseFloat(btn.dataset.speed);
    // Handle speed change
  });
});
```

### Canvas Chart Initialization

```javascript
function initChart(canvasId) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return null;
  
  const ctx = canvas.getContext('2d');
  
  // Resize canvas to container
  const container = canvas.parentElement;
  const rect = container.getBoundingClientRect();
  canvas.width = rect.width - 20;
  canvas.height = rect.height - 20;
  
  return ctx;
}

// Handle window resize
window.addEventListener('resize', () => {
  // Reinitialize charts
});
```

---

## Backend API Structure

### Flask API Setup

```python
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# Health check endpoint
@app.route('/api/health')
def health():
    return jsonify({'status': 'ok'})

# Get scenarios
@app.route('/api/scenarios')
def get_scenarios():
    return jsonify({
        'scenarios': [
            {'id': 1, 'name': 'Scenario 1', 'description': '...'},
            {'id': 2, 'name': 'Scenario 2', 'description': '...'},
        ]
    })

# Start scenario
@app.route('/api/scenario/<int:id>/start', methods=['POST'])
def start_scenario(id):
    return jsonify({
        'scenario': {...},
        'trains': [...],
        'track_info': {...}
    })

# Simulation step
@app.route('/api/simulation/step', methods=['POST'])
def simulation_step():
    return jsonify({
        'trains': [...],
        'conflicts': [...],
        'system_energy_kwh': 0
    })

# Analyze conflict
@app.route('/api/conflict/analyze', methods=['POST'])
def analyze_conflict():
    return jsonify({
        'solutions': [...],
        'energy_saved_kwh': 0
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
```

### Frontend API Calls

```javascript
const CONFIG = {
  API_BASE: 'http://localhost:5000/api',
  UPDATE_INTERVAL: 800
};

// Check backend connection
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
    addLog('error', 'Backend connection failed');
  }
}

// Load scenarios
async function loadScenarios() {
  try {
    const res = await fetch(`${CONFIG.API_BASE}/scenarios`);
    const data = await res.json();
    // Populate scenario dropdown
  } catch (error) {
    addLog('error', 'Failed to load scenarios');
  }
}

// Start scenario
async function loadScenario(id) {
  try {
    const res = await fetch(`${CONFIG.API_BASE}/scenario/${id}/start`, { 
      method: 'POST' 
    });
    const data = await res.json();
    // Initialize simulation with data
  } catch (error) {
    addLog('error', 'Failed to load scenario');
  }
}

// Simulation loop
let updateInterval = null;

function startSimulation() {
  if (updateInterval) clearInterval(updateInterval);
  
  updateInterval = setInterval(async () => {
    try {
      const res = await fetch(`${CONFIG.API_BASE}/simulation/step`, { 
        method: 'POST' 
      });
      const data = await res.json();
      
      // Update train positions
      // Check for conflicts
      // Update energy metrics
    } catch (error) {
      console.error('Simulation step failed:', error);
    }
  }, CONFIG.UPDATE_INTERVAL);
}

function stopSimulation() {
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
}
```

---

## Scrollbar Styling

```css
::-webkit-scrollbar { 
  width: 6px; 
  height: 6px; 
}

::-webkit-scrollbar-track { 
  background: var(--bg-secondary); 
}

::-webkit-scrollbar-thumb { 
  background: var(--accent-cyan); 
  border-radius: 3px; 
}

::-webkit-scrollbar-thumb:hover { 
  background: #00b8e6; 
}
```

---

## Train Markers (SVG)

```css
.train-marker {
  cursor: pointer;
  transition: transform 0.4s linear;
}

.train-marker .train-glow {
  animation: trainPulse 2s ease-in-out infinite;
}

.train-marker .train-label {
  font-family: 'JetBrains Mono', monospace;
  font-weight: bold;
  text-shadow: 0 0 4px rgba(0,0,0,0.8);
}
```

---

## File Structure

```
project/
├── frontend/
│   ├── index.html          # Main HTML structure
│   ├── styles.css          # All CSS styling
│   ├── app.js              # JavaScript logic
│   ├── delhi_junction.svg  # SVG track schematic
│   └── package.json        # Dependencies (if any)
├── backend/
│   └── api/
│       └── app.py          # Flask API server
└── DASHBOARD_DESIGN_SYSTEM.md  # This documentation
```

---

## Quick Start

1. **Start Backend:**
   ```bash
   python backend/api/app.py
   ```

2. **Start Frontend:**
   ```bash
   cd frontend
   python -m http.server 8080
   ```

3. **Open Browser:**
   Navigate to `http://localhost:8080`

---

## Summary

This design system provides:

- **Dark theme** optimized for control room/dashboard use
- **Consistent color palette** with cyan/green accents
- **Modular components** that can be reused
- **Responsive layout** that adapts to screen sizes
- **Professional typography** with Inter and JetBrains Mono
- **Smooth animations** for better UX
- **Canvas-based charts** for performance
- **RESTful API structure** for backend integration

Copy the CSS variables and component styles to quickly replicate this design in any project.
