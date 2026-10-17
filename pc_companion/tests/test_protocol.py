import json
import math
from src.protocol import parse_packet, create_pong_packet, InputPacket, ProtocolError

def test_parse_valid_input_packet():
    raw = json.dumps({
        "v": 1,
        "seq": 100,
        "t": 1713000000000,
        "type": "input",
        "inputs": {
            "steering": 0.45,
            "throttle": 0.90,
            "brake": 0.10,
            "clutch": 0.0,
            "handbrake": True,
            "gear_up": False,
            "gear_down": False,
            "select": False,
            "start": True
        }
    })
    msg_type, packet = parse_packet(raw)
    assert msg_type == "input"
    assert isinstance(packet, InputPacket)
    assert math.isclose(packet.steering, 0.45, rel_tol=1e-3)
    assert math.isclose(packet.throttle, 0.90, rel_tol=1e-3)
    assert math.isclose(packet.brake, 0.10, rel_tol=1e-3)
    assert packet.handbrake is True
    assert packet.start is True
    assert packet.gear_up is False
    assert packet.seq == 100

def test_clamping_out_of_bounds_inputs():
    raw = json.dumps({
        "v": 1,
        "type": "input",
        "inputs": {
            "steering": 2.5,   # Should clamp to 1.0
            "throttle": 1.8,   # Should clamp to 1.0
            "brake": -0.5,     # Should clamp to 0.0
            "clutch": -1.0     # Should clamp to 0.0
        }
    })
    msg_type, packet = parse_packet(raw)
    assert msg_type == "input"
    assert packet.steering == 1.0
    assert packet.throttle == 1.0
    assert packet.brake == 0.0
    assert packet.clutch == 0.0

def test_version_mismatch_raises_error():
    raw = json.dumps({
        "v": 99,
        "type": "input",
        "inputs": {}
    })
    try:
        parse_packet(raw)
        assert False, "Should have raised ProtocolError"
    except ProtocolError as e:
        assert "Unsupported protocol version" in str(e)

def test_malformed_json_raises_error():
    try:
        parse_packet("{invalid json payload")
        assert False, "Should have raised ProtocolError"
    except ProtocolError:
        pass

def test_ping_and_pong():
    raw = json.dumps({
        "v": 1,
        "type": "ping",
        "t": 123456789
    })
    msg_type, timestamp = parse_packet(raw)
    assert msg_type == "ping"
    assert timestamp == 123456789

    pong_raw = create_pong_packet(timestamp)
    pong_data = json.loads(pong_raw)
    assert pong_data["v"] == 1
    assert pong_data["type"] == "pong"
    assert pong_data["t"] == 123456789
