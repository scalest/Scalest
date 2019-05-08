import React from "react";
import { useAuth } from "../context/AuthContext"
import {
    Route,
    Redirect} from "react-router-dom";

function PrivateRoute({ children, ...rest }) {
    const { isAuthed } = useAuth()
    return (
        <Route {...rest}
            render={({ location }) =>
                isAuthed ? (children) : (
                    <Redirect
                        to={{
                            pathname: "/login",
                            state: { from: location }
                        }}
                    />
                )
            }
        />
    );
}

export default PrivateRoute