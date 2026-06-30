export interface Group {
  id: number;
  name: string;
  type: GroupType;
  role: GroupRole;
  members: GroupMemberResponse[];
  isOwner: boolean;
  alias: string
  createdAt: Date;
  updatedAt: Date
}

export type GroupType =
  | 'SHOPPING'
  | 'RECIPE';

export type GroupRole =
  | 'OWNER'
  | 'ADMIN'
  | 'MEMBER';

export interface GroupNameRequest {
  id: number;
  newName: string;
}

export interface GroupRequest {
  name: string;
  type: GroupType;
}

export interface GroupMemberResponse {
  name: string;
  role: GroupRole;
  userId: number;
}

export interface GroupAliasRequest {
  groupId: number;
  alias: string;
}

