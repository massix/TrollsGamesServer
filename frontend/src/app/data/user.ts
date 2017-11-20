export class User {
  bggNick: string;
  forumNick: string;
  email: string;
  password: string;
  role: string;
  authenticationType: string;
  bggHandled: boolean = true;
  gravatarHash: string | Int32Array;
  collectionSize: number;
}
