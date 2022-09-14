## About
This is a simple file structure storage REST API application
- Corretto 11
- Spring boot
- Maven
- Postgresql
- Jpa
- Spring boot validation
- Lombok
- Docker-compose

## Deploy
### Build
Create maven package, build docker image and push it to hub</br>

    mvn clean package -DskipTests 
    docker build -t hukola/babin-disk .
    docker tag hukola/babin-disk hukola/babin-disk:{NEWTAG}
    docker push hukola/babin-disk:{NEWTAG}

Change in docker-compose.yml this string</br>

    image: hukola/babin-disk:{NEWTAG}

Copy next files to production server:  </br>

    docker-compose.yml
    schema.sql

### Run
on production server for start as daemon

    docker-compose up -d

### Restart app
Compose file have next string for always services. </br> 
It will restart containers automatically

    restart: always
## Endpoints
    /imports
**POST** method for save disk items </br>
Example body request
    
    {
        "items": [
                    {
                        "type": "FOLDER",
                        "id": "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                        "parentId": null
                    }
                    ],
        "updateDate": "2022-02-01T12:00:00Z"
    }

- type - can be FILE or FOLDER - required 
- id - must be specified, string
- parentId - element parent
- updateDate - must be specified, date ISO 8601


    /delete/{id}
**DELETE** method for delete disk item with children items</br>
Method expects a parameter *date* with date ISO 8601. </br>
Also removes child elements

    /nodes/{id}
**GET** method for get disk items with children items

    /updates
**GET** method for get last 24h items updates</br>
Method expects a parameter *date* with date ISO 8601.</br>
Changes are selected from the *date* minus 1 day

    /node/{id}/history
**GET** method for get history of item</br>
Method expects a parameters *dateStart* and *dateEnd* with date ISO 8601.</br>

## DB structure
DB name **babin-disk** (**test** for tests)

    Table disk_item

| name | type         | pk  | fk  |
|------|--------------|-----|-----|
| disk_item_id | varchar(255) | +  |     |
|   disk_item_date   | timestamp    |     |     |
|   disk_item_parent_id   | varchar(255) |     |     |
|   disk_item_size   | bigint       |     |     |
|   disk_item_type   |   varchar(255)           |     |     |
|   disk_item_url   |      varchar(255)        |     |     |

    Table disk_item_history

| name | type         | pk  | fk  |
|------|--------------|-----|-----|
| disk_item_history_id | varchar(255) | +  |     |
| disk_item_id | varchar(255) |   |     |
|   disk_item_date   | timestamp    |     |     |
|   disk_item_parent_id   | varchar(255) |     |     |
|   disk_item_size   | bigint       |     |     |
|   disk_item_type   |   varchar(255)           |     |     |
|   disk_item_url   |      varchar(255)        |     |     |
