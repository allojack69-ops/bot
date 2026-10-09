import base64, http.server, socketserver
p="/srv-composition-camera.apk"
data=base64.b64encode(open(p,"rb").read()).decode()
for i in range(0,len(data),3000):
    print("APK_CHUNK_%05d_%05d"%(i,len(data))+" "+data[i:i+3000],flush=True)
class H(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path=="/composition-camera-v02.apk":
            d=open(p,"rb").read()
            self.send_response(200); self.send_header("Content-Type","application/vnd.android.package-archive"); self.send_header("Content-Length",str(len(d))); self.end_headers(); self.wfile.write(d)
        else:
            self.send_response(200); self.send_header("Content-Type","text/plain"); self.end_headers(); self.wfile.write(b"Composition Camera v0.2 APK builder is live.")
socketserver.TCPServer(("0.0.0.0",10000),H).serve_forever()
