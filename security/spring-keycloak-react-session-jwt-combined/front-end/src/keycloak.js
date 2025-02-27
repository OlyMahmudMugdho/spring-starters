import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://localhost:8088/", // Keycloak server URL
    realm: "my-realm",
    clientId: "react-app",
});

export default keycloak;