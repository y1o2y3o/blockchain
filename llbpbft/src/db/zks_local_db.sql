create table Block
(
    block_id bigint auto_increment,
    view_number int null,
    height int null,
    parent_block_id bigint null,
    parent_hash varchar(1024) null,
    content text null,
    hash varchar(1024) null,
    aggr_sig varchar(1024) null,
    constraint Blocks_pk
        unique (block_id)
);

