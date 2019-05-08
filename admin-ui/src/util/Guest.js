import { useAuth } from "../context/AuthContext"

function Guest({ children }) {
    const { isAuthed } = useAuth()

    if (!isAuthed) return children
    else return null
}

export default Guest