import { useAuth } from '../context/AuthContext';

function Guest({ children }) {
  const { isAuthed } = useAuth();

  if (!isAuthed()) return children;
  return null;
}

export default Guest;
