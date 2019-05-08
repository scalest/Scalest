import React, { useState } from 'react'
import HttpClient from '../util/HttpClient'

const AuthContext = React.createContext()

function AuthProvider(props) {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [isAuthed, setIsAuthed] = useState(token != null)
    const headers = isAuthed ? { Authorization: `Basic ${token}` } : {}
    const [client, setClient] = useState(new HttpClient(headers))

    console.log(client)

    async function login(username, password) {
        const res = await client.post('/admin/login', { data: { username, password } });

        if (res.isOk) {
            localStorage.setItem('token', res.data.token);
            setToken(true);
            setClient(new HttpClient({ Authorization: `Basic ${res.data.token}` }));
            setIsAuthed(true);
        }

        return res;
    }

    const logout = () => {
        localStorage.removeItem('token');
        setToken(false);
        setClient(new HttpClient({}));
        setIsAuthed(false);
    }

    return (
        <AuthContext.Provider value={{ token, client, login, logout, isAuthed, }} {...props} />
    )
}

function useAuth() {
    const context = React.useContext(AuthContext)
    if (context === undefined) {
        throw new Error(`useAuth must be used within a AuthProvider`)
    }
    return context
}

export { AuthProvider, useAuth }
