import React, { useEffect, useState } from 'react';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Box from '@material-ui/core/Box'
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import HomeIcon from '@material-ui/icons/Home';
import { Healing } from '@material-ui/icons';
import PersonAddIcon from '@material-ui/icons/PersonRounded';
import AdminMenu from "./AdminMenu"
import { Link as RouterLink, useLocation, useHistory } from "react-router-dom";
import { useAuth } from "../../context/AuthContext"
import Guest from "../../util/Guest"
import Private from "../../util/Private"

const Link = React.forwardRef((props, ref) => <RouterLink innerRef={ref} {...props} />);

function LoginButton() {
    const location = useLocation()

    if (location.pathname === '/login') return null

    return (
        <Guest>
            <Button component={Link} to="/login" color="inherit">
                Login<PersonAddIcon />
            </Button>
        </Guest>
    )
}

function LogoutButton() {
    const { logout } = useAuth()
    const history = useHistory()

    function logoutUser() {
        logout()
        history.push('/')
    }

    return (
        <Button onClick={logoutUser} color="inherit">
            Logout<PersonAddIcon />
        </Button>
    )
}

function ModelMenu() {
    const [links, setLinks] = useState()
    const { client } = useAuth()

    async function fetchData() {
        const settings = { logSuccess: true, logError: true };
        const linksData = (await client.get('/admin/info/available', settings)).data;
        const linksFromData = linksData.map(link => {
            return { name: link, to: `/model/${link}` };
        });
        setLinks(linksFromData)
    }

    useEffect(() => {
        fetchData();
    }, [])

    if (!links) return null;
    return <AdminMenu name="Models" links={links} />
}

function AdminToolbar() {
    return (
        <Private>
            <AppBar position="static">
                <Toolbar>
                    <IconButton component={Link} to="/" edge="start" color="inherit" >
                        <HomeIcon />
                    </IconButton>
                    <Box flexGrow={1} />
                    <Button component={Link} to="/health" edge="start" color="inherit" >
                        <Healing /> Health
                    </Button>
                    <ModelMenu />
                    <LoginButton />
                    <LogoutButton />
                </Toolbar>
            </AppBar>
        </Private>
    )
}

export default AdminToolbar