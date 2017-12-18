import { User } from './user';

export class UsersGroups {
  groupId: number;
  userId: string;
  role: string;
}

export class Group {
  id: number;
  name: string;
  description: string;
  status = 'PUBLIC';
  members: User[] = [];
}
