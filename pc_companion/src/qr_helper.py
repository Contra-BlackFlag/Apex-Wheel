import io
from typing import Optional

try:
    import qrcode
    HAS_QRCODE = True
except ImportError:
    qrcode = None
    HAS_QRCODE = False

def create_connection_url(ip: str, port: int) -> str:
    """Creates the deep-link connection URL."""
    return f"apexwheel://connect/?ip={ip}&port={port}"

def print_terminal_qr(ip: str, port: int):
    """Prints a clear ASCII QR code to standard output for scanning from terminal."""
    if not HAS_QRCODE:
        print("[!] Note: Install 'qrcode' (pip install qrcode) to display terminal QR codes.")
        return

    url = create_connection_url(ip, port)
    try:
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=1,
            border=1
        )
        qr.add_data(url)
        qr.make(fit=True)
        print("\n[*] Scan this QR code with the ApexWheel App on your phone to connect instantly:")
        qr.print_ascii(invert=True)
        print(f"[*] Payload: {url}\n")
    except Exception as e:
        print(f"[!] Could not render terminal QR code: {e}")

def get_qr_svg(ip: str, port: int) -> Optional[str]:
    """Generates an SVG string of the QR code for embedding in the Web Dashboard."""
    if not HAS_QRCODE:
        return None

    try:
        import qrcode.image.svg
        url = create_connection_url(ip, port)
        factory = qrcode.image.svg.SvgPathImage
        img = qrcode.make(url, image_factory=factory, box_size=8, border=2)
        stream = io.BytesIO()
        img.save(stream)
        return stream.getvalue().decode("utf-8")
    except Exception:
        return None
