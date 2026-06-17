import { useEffect, useState } from 'react';
import { FaEye } from "react-icons/fa6";
import { FaEyeSlash } from "react-icons/fa6";
import { Link, useNavigate } from "react-router-dom";
import { loginUser, warmBackend } from "../../api/utils";
const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [ showPassword, setShowPassword ] = useState(false);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        warmBackend();
    }, []);

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (loading) return;
        setLoading(true);
        if (username === '' || password === '') {
            alert('Please fill in both fields');
            return;
        }
        if (username.length < 5) {
            alert('Username must be at least 5 characters long');
            return;
        }
        if (password.length < 8) {
            alert('Password must be at least 8 characters long');
            return;
        }
        try {
            await loginUser(username, password);
            navigate("/dashboard");
        } catch (error) {
            console.error('Error occurred while logging in:', error);
            alert(error instanceof Error ? error.message : "Login failed");
        }
        finally{
            setLoading(false);
        }
    };

    return (
        <div className="login-main">
            <div className="login-center">
                <h2>Welcome back!</h2>
                <p>Please enter your details</p>
                <form onSubmit={handleSubmit}>
                <input type="username" placeholder="Username" 
                    id="username"
                    name="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <div className="pass-input-div">
                    <input type={showPassword ? "text" : "password"} placeholder="Password" 
                        id="password"
                        name="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                        {showPassword ? <FaEyeSlash onClick={() => {setShowPassword(!showPassword)}} /> : <FaEye onClick={() => {setShowPassword(!showPassword)}} />}
                </div>

                <div className="login-center-options">
                    <a href="#" className="forgot-pass-link">
                    Forgot password?
                    </a>
                </div>
                <button type="submit" className="login-btn">Login</button>
                </form>
            </div>

            <p className="login-bottom-p">
                Don't have an account? <Link to="/signup">Sign Up</Link>
            </p>
            </div>
        );
    };   

export default LoginPage;
