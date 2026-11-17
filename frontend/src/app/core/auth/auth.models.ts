export interface LoginRequest { username: string; password: string; }
export interface AuthResponse { token: string; tipo: string; expiraEnSegundos: number; usuario: string; rol: string; }
export interface AuthSession { token: string; tipo: string; usuario: string; rol: string; }
