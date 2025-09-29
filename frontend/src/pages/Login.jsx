import { useDispatch } from 'react-redux';
import { loginStart, loginSuccess, loginFailure } from '../features/authSlice';
import { loginRequest } from '../api/auth';
import AuthForm from '../components/AuthForm';

export default function Login() {
    const dispatch = useDispatch();

    const handleLogin = async (credentials) => {
        dispatch(loginStart());
        try {
            const response = await loginRequest(credentials);
            dispatch(loginSuccess(response.data));
        } catch (err) {
            dispatch(loginFailure(err.response?.data?.message || 'Login failed'));
        }
    };

    return (
        <div>
            <h2>Login</h2>
            <AuthForm onSubmit={handleLogin} buttonText="Login" />
        </div>
    );
}
