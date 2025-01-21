create table nullcheck(
    class_name text not null,
    field_name text not null,
    number_of_times_null bigint not null default 0,
    number_of_times_not_null bigint not null default 0,
    CONSTRAINT class_name_field_name_unique_constraint UNIQUE (class_name, field_name)
)
