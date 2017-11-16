import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { User } from './user';

@Injectable()
export class UsersService {
    apiBase = 'http://localhost:8180';

    getAllUsers(): Observable<User[]> {
        return this.httpClient.get<User[]>(this.apiBase + '/v1/users/get');
    }

    addUser(body: User): Observable<User> {
        const headers = new HttpHeaders().append('Content-Type', 'application/json;charset=UTF-8');
        return this.httpClient.post<User>(this.apiBase + '/v1/users/add', body, {headers: headers});
    }

    removeUser(body: User): Observable<User> {
        return this.httpClient.delete<User>(this.apiBase + '/v1/users/remove/' + body.bggNick, {
            headers: new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'))
        });
    }

    constructor(private httpClient: HttpClient) {}
}
