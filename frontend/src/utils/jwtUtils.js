export function getUserIdFromToken(token) {
    if (!token) return null;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.userId || null;
    } catch (e) {
        return null;
    }
}

export function getUsernameFromToken(token) {
    if (!token) return "User";
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub;
    } catch (e) {
        console.error("Failed to decode token for username:", e);
        return "User";
    }
}

export function isTokenExpired(token) {
    if (!token) return true;
    try {
        const payloadBase64 = token.split(".")[1];
        const payload = JSON.parse(atob(payloadBase64));
        const exp = payload.exp;
        if (!exp) return false;
        return Date.now() >= exp * 1000;
    } catch (e) {
        console.error("Failed to check token expiration:", e);
        return true;
    }
}