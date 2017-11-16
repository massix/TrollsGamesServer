import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Login } from './login';
import { User } from './user';

@Injectable()
export class LoginService {
    apiBase = 'http://localhost:8180';
    constructor(private httpClient: HttpClient) {}

    login(login: Login): Observable<any> {
        return this.httpClient.post<User>(this.apiBase + '/v1/users/login', login, {observe: 'response'});
    }
}
