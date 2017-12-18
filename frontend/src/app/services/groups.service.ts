import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Group } from '../data/group';
import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';

@Injectable()
export class GroupsService {

  headers = new HttpHeaders().set('Authorization', `Bearer ${localStorage.getItem('token')}`);

  getAllGroups(): Observable<Group[]> {
    return this.httpClient.get<Group[]>(`${environment.apiBase}/v1/groups/get/all`, {headers: this.headers});
  }

  createGroup(group: Group): Observable<Group> {
    return this.httpClient.post<Group>(`${environment.apiBase}/v1/groups/create`, group, {headers: this.headers});
  }

  constructor(private httpClient: HttpClient) {}
}
