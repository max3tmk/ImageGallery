import { useDispatch } from 'react-redux';
import { loginStart, loginSuccess, loginFailure } from '../features/authSlice';
import { registerRequest } from '../api/auth';
import AuthForm from '../components/AuthForm';

export default function Register() {
    const dispatch = useDispatch();

    const handleRegister = async (data) => {
        dispatch(loginStart());
        try {
            const response = await registerRequest(data);
            dispatch(loginSuccess(response.data));
        } catch (err) {
            dispatch(loginFailure(err.response?.data?.message || 'Register failed'));
        }
    };

    return (
        <div>
            <h2>Register</h2>
            <AuthForm onSubmit={handleRegister} buttonText="Register" />
        </div>
    );
}
