import { Injectable } from '@angular/core';
import { Login } from './login';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { User } from './user';
import { HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class LoginService {
    apiBase = 'http://localhost:8180';
    loggedUser: User;

    constructor(private httpClient: HttpClient) {}

    login(login: Login): Observable<any> {
        return this.httpClient.post<User>(this.apiBase + '/v1/users/login', login, {observe: 'response'});
    }
}
