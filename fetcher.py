import urllib.request,base64,time,os,http.server,socketserver
url="https://composition-camera-apk-builder.onrender.com/composition-camera-v02.apk"
data=urllib.request.urlopen(url,timeout=120).read()
b=base64.b64encode(data).decode()
print("APK_SIZE",len(data),flush=True)
for i in range(0,len(b),1800):
    print("APKDATA %06d "%i+b[i:i+1800],flush=True)
class H(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200); self.end_headers(); self.wfile.write(b"ok")
socketserver.TCPServer(("0.0.0.0",int(os.environ.get("PORT","10000"))),H).serve_forever()
