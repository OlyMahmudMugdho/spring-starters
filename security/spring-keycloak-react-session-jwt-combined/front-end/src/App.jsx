import { useEffect, useState } from "react";
import keycloak from "./keycloak";

const App = () => {
    const [authenticated, setAuthenticated] = useState(false);
    const [data, setData] = useState("");

    useEffect(() => {
        keycloak
            .init({ onLoad: "login-required" })
            .then((authenticated) => {
                setAuthenticated(authenticated);
                if (authenticated) {
                    fetch("http://localhost:8080/secured", { // Correct port
                        headers: {
                            Authorization: `Bearer ${keycloak.token}`, // Send JWT
                        },
                    })
                        .then((res) => res.json()) // Expect JSON, not text
                        .then(d => setData(d))
                        .catch((err) => console.error("Fetch error:", err));
                }
            })
            .catch((err) => console.error("Keycloak init error:", err));
    }, []);

    const handleLogout = () => {
        keycloak.logout({
            redirectUri: "http://localhost:5173/public",
        });
    };

    if (!authenticated) {
        return <div>Loading...</div>;
    }

    return (
        <div className="flex flex-col justify-center items-center min-h-screen">
            <h1>Welcome, {keycloak.tokenParsed?.preferred_username}</h1>
            <button className="btn btn-error" onClick={handleLogout}>Logout</button>
            <div>
                <h3>Response from back-end:</h3>
                {data && (
                    <div>
                        <h1>{data.message}</h1>
                        <p>Username: {data.username}</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default App;