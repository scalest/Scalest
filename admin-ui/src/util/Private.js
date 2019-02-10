import { useAuth } from '../context/AuthContext';

function Private({ children }) {
  const { isAuthed } = useAuth();

  if (isAuthed()) return children;
  return null;
}

export default Private;
