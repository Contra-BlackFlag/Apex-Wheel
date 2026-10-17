import json
import socket
import os
from pathlib import Path
from typing import Dict, Any, List

DEFAULT_CONFIG: Dict[str, Any] = {
    "server": {
        "host": "0.0.0.0",
        "port": 9876,
        "web_port": 8080,
        "watchdog_timeout_ms": 500,
        "max_clients": 1
    },
    "mappings": {
        "steering_axis": "left_joystick_x",
        "throttle_axis": "right_trigger",
        "brake_axis": "left_trigger",
        "clutch_button": "X",
        "handbrake_button": "A",
        "gear_up_button": "RIGHT_SHOULDER",
        "gear_down_button": "LEFT_SHOULDER",
        "select_button": "BACK",
        "start_button": "START"
    }
}

class CompanionConfig:
    def __init__(self, config_path: str = "config.json"):
        self.config_path = Path(config_path)
        self.data: Dict[str, Any] = self._load()

    def _load(self) -> Dict[str, Any]:
        if self.config_path.exists():
            try:
                with open(self.config_path, "r", encoding="utf-8") as f:
                    loaded = json.load(f)
                    # Merge with default in case keys are missing
                    merged = DEFAULT_CONFIG.copy()
                    merged["server"].update(loaded.get("server", {}))
                    merged["mappings"].update(loaded.get("mappings", {}))
                    return merged
            except Exception as e:
                print(f"Warning: Failed to load config ({e}), using defaults.")
        return json.loads(json.dumps(DEFAULT_CONFIG))

    def save(self):
        try:
            with open(self.config_path, "w", encoding="utf-8") as f:
                json.dump(self.data, f, indent=4)
        except Exception as e:
            print(f"Error saving config: {e}")

    @property
    def host(self) -> str:
        return self.data["server"]["host"]

    @property
    def port(self) -> int:
        return self.data["server"]["port"]

    @property
    def web_port(self) -> int:
        return self.data["server"]["web_port"]

    @property
    def watchdog_timeout_seconds(self) -> float:
        return self.data["server"]["watchdog_timeout_ms"] / 1000.0

    @property
    def mappings(self) -> Dict[str, str]:
        return self.data["mappings"]

    @staticmethod
    def get_local_ip_addresses() -> List[str]:
        """Discover active local IPv4 addresses to show in connection instructions."""
        ips = []
        try:
            # Connect to a dummy external address to find default interface IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.settimeout(0.1)
            s.connect(("8.8.8.8", 80))
            primary_ip = s.getsockname()[0]
            ips.append(primary_ip)
            s.close()
        except Exception:
            pass

        # Fallback to gethostbyname
        try:
            hostname = socket.gethostname()
            for ip in socket.gethostbyname_ex(hostname)[2]:
                if not ip.startswith("127.") and ip not in ips:
                    ips.append(ip)
        except Exception:
            pass

        if not ips:
            ips.append("127.0.0.1")
        return ips
