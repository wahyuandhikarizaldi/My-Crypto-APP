from http.server import BaseHTTPRequestHandler, HTTPServer
from Crypto.PublicKey import RSA

# Generate RSA key pair
key = RSA.generate(2048)
public_key = key.publickey().export_key()
private_key = key.export_key()

class RequestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()

        if self.path == "/public_key":
            # Send the public key to the client
            self.wfile.write(public_key)
        elif self.path == "/private_key":
            # Send the private key to the client
            self.wfile.write(private_key)
        else:
            # Invalid path
            self.send_error(404)

def run_server():
    host = ""
    port = 8000
    server_address = (host, port)

    httpd = HTTPServer(server_address, RequestHandler)
    print(f"Server running on {host}:{port}")

    httpd.serve_forever()

if __name__ == "__main__":
    run_server()
