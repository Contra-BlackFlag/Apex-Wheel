import json
import time
from typing import Dict, Any, Optional, Tuple

PROTOCOL_VERSION = 1

class ProtocolError(Exception):
    pass

class InputPacket:
    def __init__(
        self,
        steering: float = 0.0,
        throttle: float = 0.0,
        brake: float = 0.0,
        clutch: float = 0.0,
        handbrake: bool = False,
        gear_up: bool = False,
        gear_down: bool = False,
        select: bool = False,
        start: bool = False,
        seq: int = 0,
        timestamp: int = 0
    ):
        self.steering = max(-1.0, min(1.0, float(steering)))
        self.throttle = max(0.0, min(1.0, float(throttle)))
        self.brake = max(0.0, min(1.0, float(brake)))
        self.clutch = max(0.0, min(1.0, float(clutch)))
        self.handbrake = bool(handbrake)
        self.gear_up = bool(gear_up)
        self.gear_down = bool(gear_down)
        self.select = bool(select)
        self.start = bool(start)
        self.seq = int(seq)
        self.timestamp = int(timestamp)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "steering": self.steering,
            "throttle": self.throttle,
            "brake": self.brake,
            "clutch": self.clutch,
            "handbrake": self.handbrake,
            "gear_up": self.gear_up,
            "gear_down": self.gear_down,
            "select": self.select,
            "start": self.start,
            "seq": self.seq,
            "timestamp": self.timestamp
        }

    @classmethod
    def neutral(cls) -> "InputPacket":
        return cls(
            steering=0.0,
            throttle=0.0,
            brake=0.0,
            clutch=0.0,
            handbrake=False,
            gear_up=False,
            gear_down=False,
            select=False,
            start=False
        )


def parse_packet(line: str) -> Tuple[str, Optional[Any]]:
    """
    Parses a newline-terminated or stripped raw JSON line.
    Returns: (packet_type, payload)
    packet_type can be 'input', 'ping', or 'unknown'
    """
    if not line or not line.strip():
        raise ProtocolError("Empty packet received")

    try:
        data = json.loads(line)
    except json.JSONDecodeError as e:
        raise ProtocolError(f"Malformed JSON: {e}")

    if not isinstance(data, dict):
        raise ProtocolError("Packet must be a JSON object")

    version = data.get("v")
    if version != PROTOCOL_VERSION:
        raise ProtocolError(f"Unsupported protocol version {version}, expected {PROTOCOL_VERSION}")

    msg_type = data.get("type", "input")

    if msg_type == "ping":
        timestamp = data.get("t", int(time.time() * 1000))
        return "ping", timestamp

    elif msg_type == "input":
        inputs = data.get("inputs", {})
        if not isinstance(inputs, dict):
            raise ProtocolError("Missing or invalid 'inputs' field in packet")

        packet = InputPacket(
            steering=inputs.get("steering", 0.0),
            throttle=inputs.get("throttle", 0.0),
            brake=inputs.get("brake", 0.0),
            clutch=inputs.get("clutch", 0.0),
            handbrake=inputs.get("handbrake", False),
            gear_up=inputs.get("gear_up", False),
            gear_down=inputs.get("gear_down", False),
            select=inputs.get("select", False),
            start=inputs.get("start", False),
            seq=data.get("seq", 0),
            timestamp=data.get("t", 0)
        )
        return "input", packet

    return "unknown", data


def create_pong_packet(original_timestamp: int) -> str:
    """Builds a JSON string for pong response."""
    return json.dumps({
        "v": PROTOCOL_VERSION,
        "type": "pong",
        "t": original_timestamp
    }) + "\n"
