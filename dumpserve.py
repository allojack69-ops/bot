import base64, http.server, socketserver
from urllib.parse import urlsplit

p = "/srv-composition-camera.apk"
data = base64.b64encode(open(p, "rb").read()).decode()
for i in range(0, len(data), 3000):
    print("APK_CHUNK_%05d_%05d" % (i, len(data)) + " " + data[i:i+3000], flush=True)

class H(http.server.BaseHTTPRequestHandler):
    def send_apk(self):
        d = open(p, "rb").read()
        self.send_response(200)
        self.send_header("Content-Type", "application/vnd.android.package-archive")
        self.send_header("Content-Disposition", 'attachment; filename="Composition-Camera-v0.2.apk"')
        self.send_header("Content-Length", str(len(d)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        return d

    def do_HEAD(self):
        if urlsplit(self.path).path == "/composition-camera-v02.apk":
            d = open(p, "rb").read()
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", 'attachment; filename="Composition-Camera-v0.2.apk"')
            self.send_header("Content-Length", str(len(d)))
            self.end_headers()
        else:
            self.send_response(200)
            self.end_headers()

    def do_GET(self):
        if urlsplit(self.path).path == "/composition-camera-v02.apk":
            d = self.send_apk()
            self.wfile.write(d)
        else:
            self.send_response(200)
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.end_headers()
            self.wfile.write(b"Composition Camera v0.2 APK download endpoint.")

socketserver.TCPServer(("0.0.0.0", int(__import__("os").environ.get("PORT", "10000"))), H).serve_forever()
