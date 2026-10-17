import asyncio
import json
from src.server import ControllerServer
from src.controller.mock_output import MockGamepad
from src.protocol import InputPacket

async def test_server_socket_client_workflow():
    gamepad = MockGamepad({})
    # Use port 0 to bind on free random port
    server = ControllerServer(
        host="127.0.0.1",
        port=0,
        gamepad=gamepad,
        watchdog_timeout_seconds=1.0
    )
    await server.start()
    port = server.server.sockets[0].getsockname()[1]

    # Connect test client
    reader, writer = await asyncio.open_connection("127.0.0.1", port)

    # 1. Send Ping
    ping_payload = json.dumps({"v": 1, "type": "ping", "t": 987654321}) + "\n"
    writer.write(ping_payload.encode("utf-8"))
    await writer.drain()

    pong_line = await reader.readline()
    pong = json.loads(pong_line.decode("utf-8"))
    assert pong["v"] == 1
    assert pong["type"] == "pong"
    assert pong["t"] == 987654321

    # 2. Send Input packet
    input_payload = json.dumps({
        "v": 1,
        "type": "input",
        "inputs": {
            "steering": -0.75,
            "throttle": 1.0,
            "brake": 0.0,
            "handbrake": True
        }
    }) + "\n"
    writer.write(input_payload.encode("utf-8"))
    await writer.drain()

    # Small delay for server loop processing
    await asyncio.sleep(0.05)
    assert gamepad.last_packet.steering == -0.75
    assert gamepad.last_packet.throttle == 1.0
    assert gamepad.last_packet.handbrake is True

    # 3. Disconnect client
    writer.close()
    await writer.wait_closed()
    await asyncio.sleep(0.05)

    # Gamepad should be safely reset
    assert gamepad.last_packet.throttle == 0.0
    assert gamepad.last_packet.steering == 0.0
    assert gamepad.last_packet.handbrake is False

    await server.stop()
