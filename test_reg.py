#!/usr/bin/env python3
"""Try to register an admin user (username >=6 chars) then login."""
import requests
import json

BASE = 'http://localhost:8080'

# Try registering with a 6+ char username
print('=== Register admin user (6+ char username) ===')
r = requests.post(f'{BASE}/api/auth/register',
    json={'username': 'admin1', 'role': 'ADMIN'},
    headers={'Content-Type': 'application/json'})
print(f'Status: {r.status_code}')
try:
    print(f'Response: {json.dumps(r.json(), indent=2)}')
except:
    print(f'Response: {r.text[:500]}')

if r.status_code == 200:
    body = r.json()
    temp_pw = body.get('tempPassword', '')
    print(f'\nTemp password: {temp_pw}')
    print(f'Username: {body.get("username", "")}')
    
    # Login with the temp password
    print(f'\n=== Login with registered admin ===')
    r2 = requests.post(f'{BASE}/api/auth/login',
        json={'username': 'admin1', 'password': temp_pw},
        headers={'Content-Type': 'application/json'})
    print(f'Login status: {r2.status_code}')
    if r2.status_code == 200:
        body2 = r2.json()
        token = body2.get('token', '')
        print(f'Login OK! Token: {token[:80]}...')
        with open('admin_token.txt', 'w') as f:
            f.write(token)
        
        # Check if mustChangePassword
        user = body2.get('user', {})
        if user.get('mustChangePassword', False):
            print(f'\nMust change password! Changing to a known password...')
            uid = user.get('id')
            r3 = requests.patch(f'{BASE}/api/auth/change-password/{uid}',
                json={'oldPassword': temp_pw, 'newPassword': 'admin123'},
                headers={'Content-Type': 'application/json',
                        'Authorization': f'Bearer {token}'})
            print(f'Change password status: {r3.status_code}')
            print(f'Response: {r3.json() if r3.status_code == 200 else r3.text[:300]}')
            
            # Login with new password
            r4 = requests.post(f'{BASE}/api/auth/login',
                json={'username': 'admin1', 'password': 'admin123'},
                headers={'Content-Type': 'application/json'})
            print(f'Re-login status: {r4.status_code}')
            if r4.status_code == 200:
                new_token = r4.json().get('token', '')
                with open('admin_token.txt', 'w') as f:
                    f.write(new_token)
                print(f'New token saved!')
