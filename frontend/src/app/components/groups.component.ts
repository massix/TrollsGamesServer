import { Component, OnInit, style } from '@angular/core';
import { AlertService } from '../services/alert.service';
import { GroupsService } from '../services/groups.service';
import { Group } from '../data/group';

@Component({
  template:  `
  <div class="container">
    <div class="col-xs-12 col-sm-3">
      <div class="group-add">
        <form class="form-group" (ngSubmit)="createGroup()" name="addGroup">
          <input type="text" placeholder="name" [(ngModel)]="newGroup.name" name="groupName" />
          <input type="text" placeholder="description" [(ngModel)]="newGroup.description" name="groupDescription" />
          <select class="form-control" name="groupStatus" [(ngModel)]="newGroup.status">
            <option>PUBLIC</option>
            <option>INVITE_ONLY</option>
          </select>
          <div class="btn-group">
            <button class="btn btn-sm fa fa-plus" type="submit"></button>
          </div>
        </form>
      </div>
      <div class="group" *ngFor="let group of groups">
        <span class="group-name">({{ group.id }}) {{ group.name }}</span><br />
        <span class="group-description">{{ group.description }}</span><br />
        <!-- <span class="group-members-count">{{ group.members.length }} members</span><br /> -->
        <span class="group-status">{{ group.status }}</span>
      </div>
    </div>

    <div class="col-xs-12 col-sm-9">
      Selected group description here
    </div>
  </div>
  `,
  styles: [`
    .group {
      margin: 2px;
      border-bottom: 1px dotted black;
    }

    .group-name {
      font-size: 1.2em;
      font-weigth: 900;
    }

    .group-description {
      font-size: 0.8em;
      font-weight: 500;
    }

    .group-status {
      font-size: 0.7em;
      font-weight: 200;
    }

    .group:hover {
      background-color: rgb(231, 230, 222);
    }

    .active {
      background-color: lightgray
    }

    .group-name {

    }
  `]
})
export class GroupsComponent implements OnInit {
    groups: Group[];
    newGroup: Group = new Group();

    ngOnInit(): void {
      this.getAllGroups();
    }

    getAllGroups(): void {
      this.groupsService.getAllGroups().subscribe(data => this.groups = data);
    }

    createGroup(): void {
      console.log(`Creating group ${this.newGroup.name} (${this.newGroup.description} | ${this.newGroup.status})`);
      this.groupsService.createGroup(this.newGroup).subscribe(
        data => {
          this.getAllGroups();
          this.newGroup = new Group();
        },
        error => this.alert.error(`Could not create group (${error})`)
      );
    }

    constructor(private alert: AlertService, private groupsService: GroupsService) {}
}
