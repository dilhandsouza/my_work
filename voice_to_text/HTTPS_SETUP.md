# HTTPS Setup Guide

## Overview
Your Voice to Text application now supports both **HTTP** and **HTTPS** connections.

## URLs

### HTTP (Not Encrypted)
```
http://localhost:5000
http://<YOUR_IP>:5000
```

### HTTPS (Encrypted with Self-Signed Certificate)
```
https://localhost:5001
https://<YOUR_IP>:5001
```

## SSL Certificates

The application uses self-signed SSL certificates for HTTPS:
- **Certificate:** `cert.pem` (valid for 365 days)
- **Private Key:** `key.pem`

These were generated automatically when you ran the setup.

## Running the Server

### Start with Both HTTP and HTTPS
```bash
python app.py
```

This will:
1. ✅ Start HTTP server on **port 5000**
2. ✅ Start HTTPS server on **port 5001** (in background)

**Output will show:**
```
🌐 Access URLs:
   HTTP  (not secure):  http://localhost:5000
   HTTPS (secure):      https://localhost:5001
```

## Browser Security Warning

When accessing HTTPS, your browser will show a security warning:
```
⚠️ Your connection is not private
🔒 "This server could not prove it is secure"
```

**This is NORMAL for self-signed certificates!**

### How to Proceed (Chrome/Edge/Firefox):
1. Click **"Advanced"** or **"Details"**
2. Click **"Proceed to localhost"** or **"Accept the Risk and Continue"**
3. You'll see the app loads normally

The certificate IS valid - it's just not trusted by a Certificate Authority (CA).

## Network Access

### From Other Devices on Network

**Find Your IP Address:**
```powershell
ipconfig
```
Look for IPv4 Address (e.g., `192.168.1.100`)

**Access from Another Device:**

HTTP:
```
http://192.168.1.100:5000
```

HTTPS:
```
https://192.168.1.100:5001
```

**Accept the security warning and proceed.**

## Firewall Configuration

If you can't access from another device, allow ports through Windows Firewall:

```powershell
# Run as Administrator:
netsh advfirewall firewall add rule name="Flask Port 5000" dir=in action=allow protocol=tcp localport=5000
netsh advfirewall firewall add rule name="Flask Port 5001" dir=in action=allow protocol=tcp localport=5001
```

## Regenerating Certificates

To generate new SSL certificates:

```bash
python -c "
from cryptography import x509
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
import datetime
import ipaddress

# Generate private key
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=2048,
    backend=default_backend()
)

# Create certificate
subject = issuer = x509.Name([
    x509.NameAttribute(NameOID.COUNTRY_NAME, u'US'),
    x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, u'State'),
    x509.NameAttribute(NameOID.LOCALITY_NAME, u'Local'),
    x509.NameAttribute(NameOID.ORGANIZATION_NAME, u'Voice to Text'),
    x509.NameAttribute(NameOID.COMMON_NAME, u'localhost'),
])

cert = x509.CertificateBuilder().subject_name(
    subject
).issuer_name(
    issuer
).public_key(
    private_key.public_key()
).serial_number(
    x509.random_serial_number()
).not_valid_before(
    datetime.datetime.now(datetime.timezone.utc)
).not_valid_after(
    datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=365)
).add_extension(
    x509.SubjectAlternativeName([
        x509.DNSName(u'localhost'),
        x509.DNSName(u'*.local'),
        x509.IPAddress(ipaddress.IPv4Address(u'127.0.0.1')),
    ]),
    critical=False,
).sign(private_key, hashes.SHA256(), default_backend())

# Save certificate
with open('cert.pem', 'wb') as f:
    f.write(cert.public_bytes(serialization.Encoding.PEM))

# Save private key
with open('key.pem', 'wb') as f:
    f.write(private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.TraditionalOpenSSL,
        encryption_algorithm=serialization.NoEncryption()
    ))

print('✅ New SSL certificates generated!')
"
```

## Production Deployment

For production, use a proper SSL certificate from a Certificate Authority (CA):

- **Let's Encrypt** - Free certificates
- **Comodo/Sectigo** - Commercial certificates
- **DigiCert** - Enterprise certificates

Update the Flask configuration to use the new certificates:

```python
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
ssl_context.load_cert_chain('path/to/your/cert.pem', 'path/to/your/key.pem')
```

## Troubleshooting

### Port Already in Use
```bash
# Find what's using the port
netstat -ano | grep :5000
netstat -ano | grep :5001

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### Certificate Errors
Delete the old certificates and regenerate:
```bash
del cert.pem
del key.pem
python app.py  # This will NOT regenerate automatically
```

### Can't Access from Network
1. Check firewall rules
2. Verify both devices are on same network
3. Use correct IP address (not localhost)
4. Try HTTP port 5000 first, then HTTPS

## Security Note

⚠️ **Self-signed certificates are suitable for:**
- Local development
- Internal network use
- Testing

❌ **Do NOT use self-signed certificates for:**
- Public internet access
- Production applications
- Sensitive data transmission

For public deployment, obtain a CA-signed certificate.

## Certificate Details

Run this to view your certificate:

```bash
openssl x509 -in cert.pem -text -noout
```

It will show:
- Expiration date
- Subject (CN=localhost)
- Issuer (self-signed)
- Public key information
- Any DNS names or IPs included

---

**Need Help?** Check that:
1. ✅ SSL certificates exist (`cert.pem` and `key.pem`)
2. ✅ PyOpenSSL is installed (`pip install pyopenssl`)
3. ✅ Both ports (5000, 5001) are not blocked
4. ✅ Flask server is running
