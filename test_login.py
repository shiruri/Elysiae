#!/usr/bin/env python3
"""Try to login with various admin credentials."""
import requests
import json
import sys

BASE = 'http://localhost:8080'

# Try various admin credentials
creds = [
    ('admin','admin'),
    ('admin','admin123'),
    ('admin','password'),
    ('admin','admin12345'),
    ('admin','123456'),
    ('admin','password123'),
    ('admin','admin@123'),
    ('admin','Pokemon2626'),
    ('admin','pokemon2626'),
    ('administrator','admin'),
    ('admin','Admin123'),
    ('admin','Admin@123'),
    ('admin','Welcome1'),
    ('admin','changeme'),
    ('admin','Passw0rd'),
    ('admin','test123'),
    ('admin','letmein'),
    ('admin','qwerty'),
    ('root','root'),
    ('admin','12345678'),
    ('admin','admin1'),
    ('admin','root'),
]

print('=== Trying login with various creds ===')
for u, p in creds:
    try:
        r = requests.post(f'{BASE}/api/auth/login',
            json={'username': u, 'password': p},
            headers={'Content-Type': 'application/json'},
            timeout=5)
        msg = ''
        try:
            msg = r.json().get('message', '?')
        except:
            msg = r.text[:60]
        print(f'{u}:{p} -> {r.status_code}', end='')
        if r.status_code == 200:
            body = r.json()
            token = body.get('token', '')
            print(f' *** LOGIN OK ***')
            with open('admin_token.txt', 'w') as f:
                f.write(token)
            print(f'Token saved to admin_token.txt')
            sys.exit(0)
        else:
            print(f' ({msg[:60]})')
    except Exception as e:
        print(f'{u}:{p} -> ERROR: {e}')

print('\n=== Trying to register admin user ===')
r = requests.post(f'{BASE}/api/auth/register',
    json={'username': 'admin', 'role': 'ADMIN'},
    headers={'Content-Type': 'application/json'})
print(f'Register status: {r.status_code}')
try:
    print(f'Response: {json.dumps(r.json(), indent=2)[:300]}')
except:
    print(f'Response text: {r.text[:300]}')

print('\n=== Check if register endpoint works (unauthed) ===')
r2 = requests.post(f'{BASE}/api/auth/register',
    json={'username': 'admin2', 'role': 'ADMIN'},
    headers={'Content-Type': 'application/json',
             'Authorization': 'Bearer test'})
print(f'With bad token: {r2.status_code} - {r2.text[:200]}'  )
