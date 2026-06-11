import { useState } from 'react';
import { FaEye } from "react-icons/fa6";
import { FaEyeSlash } from "react-icons/fa6";
import { useNavigate } from "react-router-dom";
import { getResponseErrorMessage, loginUser } from "../../api/utils";

const SignUpPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [ showPassword, setShowPassword ] = useState(false);
    const navigate = useNavigate();


    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
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
            const res = await fetch("http://localhost:8080/users/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username, passwordHash: password })
            });

            if (!res.ok) {
                alert(await getResponseErrorMessage(res));
                return;
            }

            await loginUser(username, password);
            navigate("/games");
        } catch (error) {
            console.error('Error occurred while signing up:', error);
            alert(error instanceof Error ? error.message : "Signup failed");
        }
    };

    return (
        <div className="signup-main">
            <div className="signup-center">
                <h2>Welcome!</h2>
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
                </div>
                <button type="submit" className="login-btn">Sign Up</button>
                </form>
            </div>
            </div>
        );
    };   

export default SignUpPage;
