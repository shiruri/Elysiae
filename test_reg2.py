#!/usr/bin/env python3
"""Register admin user now that auth is removed from register."""
import requests
import json

BASE = 'http://localhost:8080'

# Try registering with 6+ char username
print('=== Register admin user ===')
r = requests.post(f'{BASE}/api/auth/register',
    json={'username': 'admin1', 'role': 'ADMIN'},
    headers={'Content-Type': 'application/json'})
print(f'Status: {r.status_code}')
try:
    body = r.json()
    print(f'Response: {json.dumps(body, indent=2)}')
except:
    print(f'Response: {r.text[:500]}')

if r.status_code == 200:
    temp_pw = body.get('tempPassword', '')
    print(f'\nTemp password: {temp_pw}')
    
    # Login with temp password
    print(f'\n=== Login with registered admin ===')
    r2 = requests.post(f'{BASE}/api/auth/login',
        json={'username': 'admin1', 'password': temp_pw},
        headers={'Content-Type': 'application/json'})
    print(f'Login status: {r2.status_code}')
    if r2.status_code == 200:
        body2 = r2.json()
        token = body2.get('token', '')
        with open('admin_token.txt', 'w') as f:
            f.write(token)
        print(f'Token saved!')
        
        user = body2.get('user', {})
        if user.get('mustChangePassword', False):
            uid = user.get('id')
            print(f'\nMust change password. Changing...')
            r3 = requests.patch(f'{BASE}/api/auth/change-password/{uid}',
                json={'oldPassword': temp_pw, 'newPassword': 'admin123'},
                headers={'Content-Type': 'application/json',
                        'Authorization': f'Bearer {token}'})
            print(f'Change pw status: {r3.status_code}')
            if r3.status_code == 200:
                # Login with new password
                r4 = requests.post(f'{BASE}/api/auth/login',
                    json={'username': 'admin1', 'password': 'admin123'},
                    headers={'Content-Type': 'application/json'})
                if r4.status_code == 200:
                    new_token = r4.json().get('token', '')
                    with open('admin_token.txt', 'w') as f:
                        f.write(new_token)
                    print(f'Re-login OK with admin123!')
    else:
        print(f'Login failed: {r2.text[:500]}')
