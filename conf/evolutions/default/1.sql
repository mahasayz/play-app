# --- First database schema

# --- !Ups

set ignorecase true;

create table country (
    id              bigint not null,
    code            varchar(255) not null,
    name            varchar(255),
    continent       varchar(255),
    wikipediaLink   varchar(255),
    keywords        varchar(255),
  constraint pk_country primary key (id,code))
;

create table airport (
    id              bigint not null,
    ident           varchar(255) not null,
    type            varchar(255),
    name            varchar(255),
    latitudeDeg     double,
    longitudeDeg    double,
    elevationFt     int,
    continent       varchar(255),
    isoCountry      varchar(255),
    isoRegion       varchar(255),
    municipality    varchar(255),
    scheduledService    varchar(255),
    gpsCode         varchar(255),
    iataCode        varchar(255),
    localCode       varchar(255),
    homeLink        varchar(255),
    wikipediaLink   varchar(255),
    keywords        varchar(255),
  constraint pk_airport primary key (id))
;

create table runway (
    id              bigint not null,
    airportRef      bigint not null,
    airportIdent    varchar(255),
    lengthFt        int,
    widthFt         int,
    surface         varchar(255),
    lighted         boolean,
    closed          boolean,
    leIdent         varchar(255),
    leLatitudeDeg   double,
    leLongitudeDeg  double,
    leElevationFt   int,
    leHeadingDegT   double,
    leDisplacedThresholdFt  int,
    heIdent         varchar(255),
    heLatitudeDeg   double,
    heLongitudeDeg  double,
    heElevationFt   int,
    heHeadingDegT   int,
    heDisplacedThresholdFt  int,
    constraint pk_runway primary key (id))
;

--alter table airport add constraint fk_airport_country foreign key (isoCountry) references country (code) on delete restrict on update restrict;
--alter table runway add constraint fk_runway_airport foreign key (airportRef) references airport (id) on delete restrict on update restrict;
create index ix_airport_country on airport (isoCountry);
create index ix_runway_airport on runway (airportRef);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists country;

drop table if exists airport;

drop table if exists runway;