import { useAuth } from "../context/AuthContext"

function Private({ children }) {
    const { isAuthed } = useAuth()

    if (isAuthed) return children
    else return null
}

export default Private