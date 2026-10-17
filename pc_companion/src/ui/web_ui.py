import asyncio
import json
from http.server import SimpleHTTPRequestHandler
from typing import Callable, Dict, Any

DASHBOARD_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ApexWheel PC Companion - Racing Gamepad Hub</title>
    <style>
        :root {
            --bg-color: #111215;
            --surface-color: #191b20;
            --surface-elevated: #21242b;
            --border-color: #2e323b;
            --text-primary: #e6e8ee;
            --text-secondary: #8c909e;
            --accent-green: #34d399;
            --accent-red: #f87171;
            --accent-amber: #fbbf24;
            --accent-blue: #60a5fa;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-primary);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            padding: 24px;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 20px;
            margin-bottom: 24px;
        }

        .title-block h1 {
            font-size: 24px;
            font-weight: 600;
            letter-spacing: -0.5px;
        }

        .title-block p {
            color: var(--text-secondary);
            font-size: 13px;
            margin-top: 4px;
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 500;
        }

        .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: var(--accent-red);
            transition: background-color 0.3s;
        }

        .status-dot.connected {
            background-color: var(--accent-green);
            box-shadow: 0 0 10px var(--accent-green);
        }

        .grid-container {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            flex: 1;
        }

        @media (max-width: 900px) {
            .grid-container {
                grid-template-columns: 1fr;
            }
        }

        .card {
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 24px;
        }

        .card h2 {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 20px;
            color: var(--text-primary);
            text-transform: uppercase;
            letter-spacing: 0.8px;
        }

        /* Wheel display */
        .wheel-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 280px;
        }

        .wheel {
            width: 180px;
            height: 180px;
            border-radius: 50%;
            border: 3px solid #4a4e5a;
            position: relative;
            background: rgba(255, 255, 255, 0.02);
            transition: transform 0.05s linear;
        }

        .wheel-line-h {
            position: absolute;
            top: 50%;
            left: 0;
            width: 100%;
            height: 2px;
            background: #4a4e5a;
            transform: translateY(-50%);
        }

        .wheel-line-v {
            position: absolute;
            left: 50%;
            top: 0;
            width: 2px;
            height: 100%;
            background: #4a4e5a;
            transform: translateX(-50%);
        }

        .wheel-hub {
            position: absolute;
            top: 50%;
            left: 50%;
            width: 14px;
            height: 14px;
            background: #cbd5e1;
            border-radius: 50%;
            transform: translate(-50%, -50%);
        }

        .steering-value {
            margin-top: 18px;
            font-size: 20px;
            font-weight: 600;
            font-variant-numeric: tabular-nums;
        }

        /* Pedals display */
        .pedals-container {
            display: flex;
            justify-content: space-around;
            height: 240px;
            align-items: flex-end;
            padding: 10px 0;
        }

        .pedal-bar-wrapper {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 12px;
            width: 60px;
        }

        .pedal-track {
            width: 44px;
            height: 170px;
            background: var(--surface-elevated);
            border-radius: 10px;
            border: 1px solid var(--border-color);
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
        }

        .pedal-fill {
            width: 100%;
            background: var(--text-primary);
            border-radius: 0 0 8px 8px;
            transition: height 0.04s ease-out;
            height: 0%;
        }

        .pedal-fill.brake {
            background: var(--accent-red);
        }

        .pedal-fill.clutch {
            background: var(--accent-blue);
        }

        .pedal-fill.throttle {
            background: var(--accent-green);
        }

        .pedal-label {
            font-size: 13px;
            font-weight: 500;
            color: var(--text-secondary);
        }

        /* Buttons display */
        .buttons-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 12px;
            margin-top: 20px;
        }

        .button-chip {
            background: var(--surface-elevated);
            border: 1px solid var(--border-color);
            padding: 12px;
            border-radius: 12px;
            text-align: center;
            font-size: 12px;
            font-weight: 600;
            color: var(--text-secondary);
            transition: all 0.05s ease;
        }

        .button-chip.active {
            background: var(--text-primary);
            color: var(--bg-color);
            box-shadow: 0 0 12px rgba(255, 255, 255, 0.4);
            transform: scale(0.97);
        }

        /* Info metrics */
        .metric-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            font-size: 13px;
        }

        .metric-row:last-child {
            border-bottom: none;
        }

        .metric-label {
            color: var(--text-secondary);
        }

        .metric-val {
            font-weight: 600;
            color: var(--text-primary);
            font-variant-numeric: tabular-nums;
        }

        .highlight-ip {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--border-color);
            padding: 12px 16px;
            border-radius: 10px;
            margin-top: 14px;
            font-family: monospace;
            font-size: 14px;
            color: var(--accent-amber);
            word-break: break-all;
        }
    </style>
</head>
<body>
    <header>
        <div class="title-block">
            <h1>ApexWheel Companion</h1>
            <p>PC Virtual Gamepad Engine for Racing Simulators</p>
        </div>
        <div class="status-badge">
            <div id="statusDot" class="status-dot"></div>
            <span id="statusText">Awaiting Phone...</span>
        </div>
    </header>

    <div class="grid-container">
        <!-- Live Steering Wheel Card -->
        <div class="card">
            <h2>Real-time Steering</h2>
            <div class="wheel-container">
                <div id="wheel" class="wheel">
                    <div class="wheel-line-h"></div>
                    <div class="wheel-line-v"></div>
                    <div class="wheel-hub"></div>
                </div>
                <div id="steeringDeg" class="steering-value">0.0° (Center)</div>
            </div>
        </div>

        <!-- Pedals Card -->
        <div class="card">
            <h2>Analog Controls</h2>
            <div class="pedals-container">
                <div class="pedal-bar-wrapper">
                    <div class="pedal-track">
                        <div id="clutchFill" class="pedal-fill clutch"></div>
                    </div>
                    <div class="pedal-label">Clutch</div>
                </div>
                <div class="pedal-bar-wrapper">
                    <div class="pedal-track">
                        <div id="brakeFill" class="pedal-fill brake"></div>
                    </div>
                    <div class="pedal-label">Brake</div>
                </div>
                <div class="pedal-bar-wrapper">
                    <div class="pedal-track">
                        <div id="throttleFill" class="pedal-fill throttle"></div>
                    </div>
                    <div class="pedal-label">Throttle</div>
                </div>
            </div>
        </div>

        <!-- Buttons Card -->
        <div class="card">
            <h2>Virtual Buttons</h2>
            <div class="buttons-grid">
                <div id="btnHandbrake" class="button-chip">E-BRAKE (A)</div>
                <div id="btnGearUp" class="button-chip">GEAR UP (RB)</div>
                <div id="btnGearDown" class="button-chip">GEAR DOWN (LB)</div>
                <div id="btnSelect" class="button-chip">SELECT (BACK)</div>
                <div id="btnStart" class="button-chip">START (START)</div>
            </div>
        </div>

        <!-- System & Network Diagnostics Card -->
        <div class="card">
            <h2>Driver & Network Status</h2>
            <div class="metric-row">
                <span class="metric-label">Driver Type</span>
                <span id="driverType" class="metric-val">-</span>
            </div>
            <div class="metric-row">
                <span class="metric-label">Client Address</span>
                <span id="clientAddr" class="metric-val">None</span>
            </div>
            <div class="metric-row">
                <span class="metric-label">Update Rate</span>
                <span id="updateRate" class="metric-val">0 Hz</span>
            </div>
            <div class="metric-row">
                <span class="metric-label">Total Packets</span>
                <span id="totalPackets" class="metric-val">0</span>
            </div>
            <div class="metric-row">
                <span class="metric-label">Disconnect Watchdog</span>
                <span class="metric-val" style="color: var(--accent-green)">Active (500ms failsafe)</span>
            </div>

            <div style="margin-top: 14px; font-size: 12px; color: var(--text-secondary);">
                Enter one of these IP addresses in the Android App:
            </div>
            <div id="ipList" class="highlight-ip">Detecting local IPs...</div>
        </div>
    </div>

    <script>
        const statusDot = document.getElementById("statusDot");
        const statusText = document.getElementById("statusText");
        const wheel = document.getElementById("wheel");
        const steeringDeg = document.getElementById("steeringDeg");
        const clutchFill = document.getElementById("clutchFill");
        const brakeFill = document.getElementById("brakeFill");
        const throttleFill = document.getElementById("throttleFill");
        const btnHandbrake = document.getElementById("btnHandbrake");
        const btnGearUp = document.getElementById("btnGearUp");
        const btnGearDown = document.getElementById("btnGearDown");
        const btnSelect = document.getElementById("btnSelect");
        const btnStart = document.getElementById("btnStart");
        const driverType = document.getElementById("driverType");
        const clientAddr = document.getElementById("clientAddr");
        const updateRate = document.getElementById("updateRate");
        const totalPackets = document.getElementById("totalPackets");
        const ipList = document.getElementById("ipList");

        async function pollState() {
            try {
                const res = await fetch("/api/state");
                if (res.ok) {
                    const data = await res.json();
                    render(data);
                }
            } catch (e) {
                statusText.innerText = "Connecting to companion...";
                statusDot.classList.remove("connected");
            }
        }

        function render(data) {
            // Status
            if (data.connected) {
                statusDot.classList.add("connected");
                statusText.innerText = "Phone Connected (" + data.rate_hz + " Hz)";
            } else {
                statusDot.classList.remove("connected");
                statusText.innerText = "Listening for Phone on Port " + (data.port || 9876);
            }

            // Wheel
            const steering = data.inputs.steering || 0.0;
            const angle = (steering * 45.0).toFixed(1);
            wheel.style.transform = `rotate(${angle}deg)`;
            steeringDeg.innerText = `${angle}° (${(steering * 100).toFixed(0)}%)`;

            // Pedals
            clutchFill.style.height = (data.inputs.clutch * 100).toFixed(0) + "%";
            brakeFill.style.height = (data.inputs.brake * 100).toFixed(0) + "%";
            throttleFill.style.height = (data.inputs.throttle * 100).toFixed(0) + "%";

            // Buttons
            btnHandbrake.classList.toggle("active", !!data.inputs.handbrake);
            btnGearUp.classList.toggle("active", !!data.inputs.gear_up);
            btnGearDown.classList.toggle("active", !!data.inputs.gear_down);
            btnSelect.classList.toggle("active", !!data.inputs.select);
            btnStart.classList.toggle("active", !!data.inputs.start);

            // Metrics
            driverType.innerText = data.gamepad.type || "Active";
            clientAddr.innerText = data.client_address || "None";
            updateRate.innerText = data.rate_hz + " Hz";
            totalPackets.innerText = data.total_packets;

            if (data.local_ips && data.local_ips.length > 0) {
                ipList.innerText = data.local_ips.join("  |  ") + " (Port: " + data.port + ")";
            }
        }

        setInterval(pollState, 33); // 30fps refresh
        pollState();
    </script>
</body>
</html>
"""

class WebDashboardServer:
    """Lightweight HTTP server providing web dashboard and JSON state API."""

    def __init__(self, host: str, port: int, get_state_callback: Callable[[], Dict[str, Any]]):
        self.host = host
        self.port = port
        self.get_state = get_state_callback
        self.server: Optional[asyncio.Server] = None

    async def start(self):
        self.server = await asyncio.start_server(
            self._handle_http,
            self.host,
            self.port
        )
        print(f"[*] Web Control Panel available at http://{self.host if self.host != '0.0.0.0' else 'localhost'}:{self.port}")

    async def stop(self):
        if self.server:
            self.server.close()
            await self.server.wait_closed()

    async def _handle_http(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        try:
            request_line = await reader.readline()
            if not request_line:
                return

            req_str = request_line.decode("utf-8", errors="ignore")
            parts = req_str.split()
            if len(parts) < 2:
                return

            path = parts[1]

            # Read remaining headers
            while True:
                line = await reader.readline()
                if not line or line == b"\r\n" or line == b"\n":
                    break

            if path == "/api/state":
                state = self.get_state()
                body = json.dumps(state).encode("utf-8")
                response = (
                    b"HTTP/1.1 200 OK\r\n"
                    b"Content-Type: application/json\r\n"
                    b"Access-Control-Allow-Origin: *\r\n"
                    b"Content-Length: " + str(len(body)).encode("ascii") + b"\r\n"
                    b"Connection: close\r\n\r\n" + body
                )
            else:
                body = DASHBOARD_HTML.encode("utf-8")
                response = (
                    b"HTTP/1.1 200 OK\r\n"
                    b"Content-Type: text/html; charset=utf-8\r\n"
                    b"Content-Length: " + str(len(body)).encode("ascii") + b"\r\n"
                    b"Connection: close\r\n\r\n" + body
                )

            writer.write(response)
            await writer.drain()
        except Exception:
            pass
        finally:
            try:
                writer.close()
            except Exception:
                pass
