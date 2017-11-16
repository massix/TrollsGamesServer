import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Login } from '../data/login';
import { User } from '../data/user';
import { Server } from '../constants/server';

@Injectable()
export class LoginService {
    apiBase = Server.apiBase;
    constructor(private httpClient: HttpClient) {}

    login(login: Login): Observable<any> {
        return this.httpClient.post<User>(this.apiBase + '/v1/users/login', login, {observe: 'response'});
    }
}
