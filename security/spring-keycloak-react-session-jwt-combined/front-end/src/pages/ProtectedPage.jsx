import { useState, useEffect } from "react";
import axios from "axios";
import keycloak from "../keycloak";

function ProtectedPage({ authenticated }) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (authenticated) {
            const fetchData = async () => {
                try {
                    const response = await axios.get("http://localhost:8080/secured", {
                        headers: {
                            Authorization: `Bearer ${keycloak.token}`,
                        },
                    });
                    setData(response.data);
                } catch (err) {
                    setError("Failed to fetch secured data.");
                }
            };
            fetchData();
        } else {
            setError("Please log in to access this page.");
        }
    }, [authenticated]);

    return (
        <div className="hero min-h-screen bg-base-200">
            <div className="hero-content text-center">
                <div className="max-w-md">
                    <h1 className="text-5xl font-bold">Protected Page</h1>
                    {data ? (
                        <div>
                            <p className="py-6">{data.message}</p>
                            <p className="py-2">Welcome, {data.username}!</p>
                        </div>
                    ) : error ? (
                        <div>
                            <p className="py-6 text-red-500">{error}</p>
                            {!authenticated && (
                                <button onClick={() => keycloak.login()} className="btn btn-primary">
                                    Log In
                                </button>
                            )}
                        </div>
                    ) : (
                        <p className="py-6">Loading...</p>
                    )}
                </div>
            </div>
        </div>
    );
}

export default ProtectedPage;