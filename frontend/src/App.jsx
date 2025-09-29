import { Provider } from 'react-redux';
import { store } from './app/store';
import Login from './pages/Login';
import Register from './pages/Register';

function App() {
    return (
        <Provider store={store}>
            <div>
                <h1>Authentication Service</h1>
                <Login />
                <Register />
            </div>
        </Provider>
    );
}

export default App;
