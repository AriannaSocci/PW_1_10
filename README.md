# Agricultural Data Generator & Dashboard

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-22-orange.svg)](https://openjdk.java.net/)

A comprehensive Spring Boot application for simulating, storing, and visualizing agricultural environmental and production data. Built for real-time monitoring and analysis of farming operations across multiple Italian regions.

---

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Data Models](#data-models)
- [WebSocket Events](#websocket-events)
- [Dashboard Features](#dashboard-features)
- [Data Simulation](#data-simulation)
- [Development](#development)
- [Additional Resources](#additional-resources)
---

## Features

### Core Features

- **Real-time Data Simulation**: Generate realistic environmental and production data for 8 Italian agricultural regions
- **Live WebSocket Updates**: Real-time dashboard updates as new data is generated
- **Interactive Dashboard**: Visualize environmental metrics and production data with dynamic charts
- **Time Range Filtering**: View historical data within custom time ranges
- **Production Analytics**: Track crop yields, revenue, efficiency, and costs across regions
- **Regional Climate Modeling**: Accurate seasonal variations for Italian regions (Lombardia, Piemonte, Toscana, etc.)
- **Multi-Crop Support**: 5 different crops with seasonal growth patterns (Wheat, Corn, Rice, Soy, Sunflower)

### Dashboard Capabilities

- **Environmental Monitoring**:
  - Temperature tracking with seasonal variations
  - Humidity and precipitation monitoring
  - Soil moisture levels with realistic evaporation/rain dynamics
  - Wind speed measurements

- **Production Analytics**:
  - Harvest quantity tracking (kg)
  - Yield per hectare analysis (kg/ha)
  - Production efficiency metrics (%)
  - Revenue and cost tracking (€)
  - Crop distribution visualization (pie charts)

- **Interactive Controls**:
  - Region and metric filtering
  - Live mode with real-time updates
  - Custom time range selection
  - Auto-select most recent data
  - Export-ready visualizations

---

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Controller  │  │   Service    │  │  Repository  │      │
│  │    Layer     │──│    Layer     │──│    Layer     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         │                  │                  ▼              │
│         │                  │          ┌──────────────┐      │
│         │                  │          │  H2 Database │      │
│         │                  │          │   (In-Memory)│      │
│         │                  └──────────┴──────────────┘      │
│         │                  │                                 │
│         │                  ▼                                 │
│         │          ┌──────────────┐                         │
│         │          │  WebSocket   │                         │
│         │          │   Scheduler  │                         │
│         │          └──────────────┘                         │
│         │                  │                                 │
│         ▼                  ▼                                 │
│  ┌────────────────────────────────┐                         │
│  │     Thymeleaf Templates        │                         │
│  │  (Dashboard + Simulate Pages)  │                         │
│  └────────────────────────────────┘                         │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
              ┌──────────────────┐
              │   Web Browser    │
              │  (Client Side)   │
              │                  │
              │  • Chart.js      │
              │  • STOMP.js      │
              │  • SockJS        │
              └──────────────────┘
```

### Package Structure

```
org.ancora_casini.PW_1_10/
├── DataGeneratorApplication.java    # Main Spring Boot application
├── config/
│   └── WebSocketConfig.java         # WebSocket configuration
├── controller/
│   └── DashboardController.java     # HTTP request handlers
├── model/
│   ├── BaseData.java                # Abstract base class
│   ├── EnvironmentalData.java       # Environmental metrics entity
│   ├── ProductionData.java          # Production metrics entity
│   ├── Crop.java                    # Crop enumeration with seasons
│   ├── Region.java                  # Italian regions enumeration
│   ├── TimeUnit.java                # Time interval units
│   └── Interval.java                # Data generation interval
├── repository/
│   ├── EnvironmentalDataRepository.java  # Environmental data persistence
│   └── ProductionDataRepository.java     # Production data persistence
└── service/
    ├── DataSimulatorService.java    # Data generation logic
    ├── DashboardService.java        # Dashboard data aggregation
    └── WebSocketDataService.java   # Real-time data broadcasting
```

---

## Technology Stack

### Backend

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 22
- **Persistence**: 
  - Spring Data JPA
  - Hibernate 6.6.29
  - H2 Database (in-memory)
- **Real-time Communication**: 
  - WebSocket (STOMP over SockJS)
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (code generation)
  - Spring Boot Actuator (monitoring)

### Frontend

- **Visualization**: Chart.js 4.x
- **Real-time**: STOMP.js + SockJS
- **Styling**: Custom CSS with CSS Variables
- **Architecture**: Vanilla JavaScript (ES6+)

### Database

- **Type**: H2 (in-memory)
- **Mode**: Development/Testing
- **Console**: Available at `/h2-console`
- **Schema**: Auto-created on startup

---

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java JDK 22** or higher
  ```bash
  java -version
  # Should output: java version "22.x.x"
  ```

- **Maven 3.8+**
  ```bash
  mvn -version
  # Should output: Apache Maven 3.8.x or higher
  ```

- **Git** (for cloning the repository)
  ```bash
  git --version
  ```

### Optional Tools

- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- **HTTP Client**: Postman, curl, or browser DevTools
- **Browser**: Modern browser with WebSocket support (Chrome, Firefox, Safari, Edge)

---

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/data_generator.git
cd data_generator
```

### 2. Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the source code
- Run tests (if any)
- Create a JAR file in `target/`

### 3. Run the Application

#### Option A: Using Maven

```bash
mvn spring-boot:run
```

#### Option B: Using the JAR

```bash
java -jar target/data_generator-1.0.0.jar
```

#### Option C: Using IDE

- Open the project in your IDE
- Run `DataGeneratorApplication.java` main method

### 4. Verify Installation

Open your browser and navigate to:
- **Main Page**: http://localhost:8080/
- **Dashboard**: http://localhost:8080/dashboard
- **H2 Console**: http://localhost:8080/h2-console

---

## Configuration

### Application Properties

Configuration is managed in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: data_generator

  # Database Configuration
  datasource:
    url: jdbc:h2:mem:data_generator_db
    driver-class-name: org.h2.Driver
    username: sa
    password: password

  h2:
    console:
      enabled: true
      path: /h2-console

  # JPA Configuration
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop    # Recreates schema on each startup
    show-sql: true             # Logs SQL queries

# Server Configuration
server:
  port: 8080
```

### Customization Options

#### Change Server Port

```yaml
server:
  port: 9090  # Change to your desired port
```

#### Use Persistent Database

Replace H2 in-memory with file-based:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/data_generator_db
    # Data will be persisted in ./data/ directory
```

#### Disable SQL Logging

```yaml
spring:
  jpa:
    show-sql: false
```

---

## Usage

### Quick Start Guide

#### 1. Generate Sample Data

1. Open http://localhost:8080/ (Simulate page)
2. Click **"Quick Preset (Full Year)"** button
   - This generates data for the entire year 2025
   - Interval: 6 hours
   - Both environmental and production data
3. Click **"Generate Data"**
4. Wait for generation to complete (redirects to dashboard)

#### 2. View Dashboard

Navigate to http://localhost:8080/dashboard

**Controls:**
- **Region Selector**: Choose from 8 Italian regions
- **Metric Selector**: Select environmental metric to visualize
- **Live Mode**: 
  - **Checked**: Shows all data with real-time updates
  - **Unchecked**: Allows custom time range filtering
- **Time Range**: Set start/end dates (only in Range mode)
- **Apply Range**: Filter data by selected time window
- **Auto-select Most Recent**: Jump to region with latest data
- **Reset**: Clear filters and return to default view

### Advanced Usage

#### Custom Data Generation

1. Navigate to http://localhost:8080/
2. Configure generation parameters:
   - **Data Type**: Environmental, Production, or Both
   - **Start Time**: Beginning of time range
   - **End Time**: End of time range
   - **Interval Value**: Number (e.g., 5)
   - **Interval Unit**: Minutes, Hours, Days, Weeks, Months
3. Click **"Generate Data"**

#### Example Scenarios

**Generate Last 7 Days (Hourly)**
- Start: 2025-11-09 00:00
- End: 2025-11-16 23:59
- Interval: 1 Hour
- Data Type: Both

**Generate Summer Season (Daily)**
- Start: 2025-06-01 00:00
- End: 2025-08-31 23:59
- Interval: 1 Day
- Data Type: Production

**Generate High-Frequency Data (5 minutes)**
- Start: 2025-11-16 00:00
- End: 2025-11-16 23:59
- Interval: 5 Minutes
- Data Type: Environmental

---

## API Endpoints

### Web Pages

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Simulate page (data generation form) |
| GET | `/simulate` | Alias for `/` |
| GET | `/dashboard` | Main dashboard with charts |
| GET | `/dashboard?start=<ISO>&end=<ISO>` | Dashboard with time filter |

### Data Generation

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/simulate-data` | Generate simulated data | Form data (see below) |

**Form Parameters:**
```
dataType: environmental | production | both
start: ISO 8601 DateTime (e.g., 2025-01-01T00:00:00Z)
end: ISO 8601 DateTime
intervalValue: Integer (e.g., 6)
intervalUnit: MINUTES | HOURS | DAYS | WEEKS | MONTHS
```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8080/simulate-data \
  -d "dataType=both" \
  -d "start=2025-01-01T00:00:00Z" \
  -d "end=2025-12-31T23:59:59Z" \
  -d "intervalValue=6" \
  -d "intervalUnit=HOURS"
```

### WebSocket

| Protocol | Endpoint | Description |
|----------|----------|-------------|
| STOMP | `/ws` | WebSocket connection endpoint |
| Topic | `/topic/dashboard-updates` | Subscribe for real-time data updates |

**WebSocket Connection Example (JavaScript):**
```javascript
const socket = new SockJS('/ws');
const client = Stomp.over(socket);

client.connect({}, function() {
    client.subscribe('/topic/dashboard-updates', function(message) {
        const data = JSON.parse(message.body);
        console.log('New data received:', data);
    });
});
```

### H2 Database Console

| Endpoint | Credentials |
|----------|-------------|
| URL | http://localhost:8080/h2-console |
| JDBC URL | `jdbc:h2:mem:data_generator_db` |
| Username | `sa` |
| Password | `password` |

---

## Data Models

### Environmental Data

Represents environmental conditions at a specific time and location.

**Entity:** `EnvironmentalData`

| Field | Type | Unit | Description | Range |
|-------|------|------|-------------|-------|
| `id` | Long | - | Primary key | Auto-generated |
| `timestamp` | OffsetDateTime | - | Data collection time | ISO 8601 |
| `region` | Region (Enum) | - | Italian region | 8 regions |
| `temperature` | Double | °C | Air temperature | -10 to 40 |
| `humidity` | Double | % | Relative humidity | 0 to 100 |
| `precipitation` | Double | mm | Rainfall amount | 0 to 50 |
| `soilMoisture` | Double | % | Soil water content | 10 to 100 |
| `windSpeed` | Double | km/h | Wind velocity | 0 to 100 |

**Example JSON:**
```json
{
  "id": 1,
  "timestamp": "2025-01-01T06:00:00+01:00",
  "region": "LOMBARDIA",
  "temperature": 5.2,
  "humidity": 78.5,
  "precipitation": 2.3,
  "soilMoisture": 65.4,
  "windSpeed": 12.8
}
```

### Production Data

Represents agricultural production metrics for a crop in a region.

**Entity:** `ProductionData`

| Field | Type | Unit | Description |
|-------|------|------|-------------|
| `id` | Long | - | Primary key |
| `timestamp` | OffsetDateTime | - | Harvest/measurement time |
| `region` | Region (Enum) | - | Italian region |
| `crop` | Crop (Enum) | - | Crop type |
| `harvestQuantity` | Double | kg | Total harvest amount |
| `growthDays` | Integer | days | Days from planting to harvest |
| `yieldPerHectare` | Double | kg/ha | Productivity per hectare |
| `productionCost` | Double | € | Total production expenses |
| `marketPrice` | Double | €/kg | Current market price |
| `revenue` | Double | € | Total income from sales |
| `efficiency` | Double | % | Production efficiency rating |

**Example JSON:**
```json
{
  "id": 1,
  "timestamp": "2025-06-15T12:00:00+02:00",
  "region": "EMILIA_ROMAGNA",
  "crop": "WHEAT",
  "harvestQuantity": 5240.8,
  "growthDays": 180,
  "yieldPerHectare": 4320.5,
  "productionCost": 1850.0,
  "marketPrice": 0.25,
  "revenue": 1310.2,
  "efficiency": 85.3
}
```

### Regions

**Enum:** `Region`

| Value | Description | Climate Characteristics |
|-------|-------------|-------------------------|
| `LOMBARDIA` | Lombardy | Continental, cold winters, humid autumns |
| `PIEMONTE` | Piedmont | Alpine influence, cold winters |
| `TOSCANA` | Tuscany | Mediterranean, warm summers |
| `EMILIA_ROMAGNA` | Emilia-Romagna | Continental, moderate |
| `VENETO` | Veneto | Continental with Adriatic influence |
| `SICILIA` | Sicily | Mediterranean, hot dry summers |
| `CAMPANIA` | Campania | Mediterranean, mild |
| `LAZIO` | Lazio | Mediterranean, warm |

### Crops

**Enum:** `Crop`

| Value       | Growth Season | Typical Harvest | Growing Days |
|-------------|---------------|-----------------|--------------|
| `WHEAT`     | Autumn-Spring | June | 180-210 |
| `CORN`      | Spring-Summer | September | 120-150 |
| `RICE`      | Spring-Summer | October | 150-180 |
| `SOY`       | Spring-Summer | September | 120-150 |
| `SUNFLOWER` | Spring-Summer | August | 90-120 |

---

## WebSocket Events

### Connection Flow

```
1. Client opens SockJS connection to /ws
2. Client upgrades to STOMP protocol
3. Client subscribes to /topic/dashboard-updates
4. Server broadcasts updates every 5 seconds (when data changes)
5. Client receives JSON payload with new data
```

### Message Format

**Topic:** `/topic/dashboard-updates`

**Payload Structure:**
```json
{
  "lastUpdate": "2025-11-16T15:30:00+01:00",
  "environmentalData": {
    "LOMBARDIA": [
      {
        "timestamp": "2025-11-16T15:30:00+01:00",
        "temperature": 8.5,
        "humidity": 72.3,
        "precipitation": 0.0,
        "soilMoisture": 45.2,
        "windSpeed": 15.2
      }
    ],
    "PIEMONTE": [ /* ... */ ]
  },
  "productionData": [
    {
      "timestamp": "2025-11-16T12:00:00+01:00",
      "region": "TOSCANA",
      "crop": "WHEAT",
      "harvestQuantity": 4280.5,
      "yieldPerHectare": 3850.0,
      "efficiency": 82.5,
      "revenue": 1070.12
    }
  ]
}
```

### Client-Side Handling

```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to dashboard updates
    stompClient.subscribe('/topic/dashboard-updates', function(message) {
        const payload = JSON.parse(message.body);
        
        // Update environmental charts
        updateEnvironmentalCharts(payload.environmentalData);
        
        // Update production charts
        updateProductionCharts(payload.productionData);
        
        // Show notification
        showToast(`${payload.lastUpdate}: New data received`);
    });
});
```

---

## Dashboard Features

### Environmental Chart

**Location:** Left side of dashboard

**Features:**
- Line chart showing temporal evolution
- Real-time updates (Live mode)
- Time range filtering (Range mode)
- Metric selection: Temperature, Humidity, Precipitation, Soil Moisture, Wind Speed
- Region filtering with search
- Auto-select most recent data
- Hover tooltips with precise values

**Statistics Panel:**
- Data point count
- Average value
- Minimum value
- Maximum value
- Last update timestamp

### Production Pie Chart

**Location:** Bottom of dashboard

**Features:**
- Shows distribution of selected metric across all crops in a region
- Interactive legend (click to toggle crops)
- Percentage display in tooltips
- Color-coded by crop type
- Metric selection: Harvest Quantity, Yield, Efficiency, Revenue, Cost

**Crop Colors:**
- WHEAT: Gold (#FFD700)
- CORN: Dark Orange (#FF8C00)
- BARLEY: Medium Purple (#9370DB)
- RICE: Light Sea Green (#20B2AA)
- SOY: Lime Green (#32CD32)
- SUNFLOWER: Yellow (#FFFF00)

### Live Mode vs Range Mode

| Feature | Live Mode | Range Mode |
|---------|--------|--------|
| Data Display | All available data | Filtered by time range |
| Real-time Updates | Yes | No |
| WebSocket | Active | Disconnected |
| Time Controls | Disabled | Active |
| Use Case | Monitoring current operations | Historical analysis |

---

## Data Simulation

### Simulation Algorithm

The data generator uses advanced algorithms to create realistic agricultural data:

#### Environmental Data Generation

**Temperature:**
```
base = region.averageTemperature
seasonal = sin(monthOfYear) * seasonalAmplitude
dailyVariation = sin(hourOfDay) * 3°C
random = gaussian(μ=0, σ=2)
temperature = base + seasonal + dailyVariation + random
```

**Humidity:**
```
base = 50%
seasonal = summer → -10%, winter → +5%
correlation = -0.3 * (temperature - 20)
precipitation_effect = +8% per mm of rain
random = gaussian(μ=0, σ=5)
humidity = clamp(base + seasonal + correlation + precipitation_effect + random, 20, 100)
```

**Soil Moisture:**
```
gain_from_rain = precipitation * 1.5
loss_from_evaporation = (100 - humidity) * 0.1
target = humidity * 0.5 + gain_from_rain - loss_from_evaporation
soilMoisture = 0.80 * previous + 0.15 * target + gaussian(μ=0, σ=3)
```

**Key Features:**
- Seasonal patterns (summer dry, winter wet)
- Regional climate differences (North vs South Italy)
- Realistic correlations (temperature ↔ humidity)
- Weather events (rain increases soil moisture)
- Gradual transitions (no sudden jumps)
- Random variations (Gaussian noise)

#### Production Data Generation

**Harvest Quantity:**
```
basePlanting = region.baseHectares
seasonal = crop.isInSeason(month) ? 1.0 : 0.3
weather = avg(soilMoisture, humidity) / 100
efficiency = uniform(0.75, 0.95)
quantity = basePlanting * 4000 * seasonal * weather * efficiency
```

**Revenue Calculation:**
```
marketPrice = crop.basePrice * (1 + gaussian(μ=0, σ=0.15))
revenue = harvestQuantity * marketPrice
efficiency = (revenue / productionCost) * 100
```

**Key Features:**
- Crop-specific growing seasons
- Weather impact on yields
- Market price fluctuations
- Cost-benefit analysis
- Regional productivity differences

### Seasonal Variations

| Season | Months | Environmental Effects | Production Effects |
|--------|--------|----------------------|-------------------|
| **Spring** | Mar-May | Warming temperatures, moderate rain | Planting season, high activity |
| **Summer** | Jun-Aug | Hot, dry (especially South), low soil moisture | Peak growth, irrigation critical |
| **Autumn** | Sep-Nov | Cooling, increased rain (North), fog | Harvest season, wet conditions |
| **Winter** | Dec-Feb | Cold (North), mild (South), high moisture | Dormant period, winter crops |

---

## Development

### Project Structure

```
data_generator/
├── src/
│   ├── main/
│   │   ├── java/org/ancora_casini/data_generator/
│   │   │   ├── DataGeneratorApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── static/
│   │       │   └── js/
│   │       │       ├── chart.min.js
│   │       │       ├── sockjs.min.js
│   │       │       └── stomp.min.js
│   │       └── templates/
│   │           ├── dashboard.html
│   │           └── simulate.html
│   └── test/
│       └── java/org/ancora_casini/data_generator/
├── target/                    # Build output
├── pom.xml                    # Maven configuration
└── README.md                  # This file
```

### Building from Source

```bash
# Clean build
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn package

# Skip tests during build
mvn package -DskipTests

# Clean install
mvn clean install
```

### Running in Development Mode

```bash
# With auto-reload (requires spring-boot-devtools)
mvn spring-boot:run

# With debug port
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Code Style

- **Java**: Follow standard Java conventions
- **Naming**: 
  - Classes: PascalCase
  - Methods/Variables: camelCase
  - Constants: UPPER_SNAKE_CASE
- **Formatting**: 4-space indentation
- **Comments**: Javadoc for public APIs

### Adding New Features

#### Add a New Environmental Metric

1. **Update Model** (`EnvironmentalData.java`):
```java
@Column
private Double newMetric;
```

2. **Update Simulator** (`DataSimulatorService.java`):
```java
state.newMetric = calculateNewMetric(state);
```

3. **Update Dashboard** (`dashboard.html`):
```javascript
const metricConfig = {
    // ...existing metrics...
    newMetric: { label: 'New Metric', color: 'rgba(255,0,0,1)', decimals: 2 }
};
```

#### Add a New Crop

1. **Update Enum** (`Crop.java`):
```java
NEWCROP(Month.MARCH, Month.SEPTEMBER, "New Crop")
```

2. **Update Colors** (dashboard.html, line ~680):
```javascript
const cropColors = {
    // ...existing crops...
    NEWCROP: '#HEX_COLOR'
};
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=DataGeneratorApplicationTests

# Integration tests
mvn verify
```

---
## Additional Resources

### Documentation

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf](https://www.thymeleaf.org/documentation.html)
- [Chart.js](https://www.chartjs.org/docs/latest/)
- [STOMP Protocol](https://stomp.github.io/)

### Related Files

- `SOIL_MOISTURE_FIX_SUMMARY.md` - Details on soil moisture algorithm improvements
- `PIE_CHART_IMPLEMENTATION_SUMMARY.md` - Production pie chart feature documentation
- `DEFAULT_PAGE_CHANGES_SUMMARY.md` - Navigation and routing changes

### Project History

**Version 1.0.0**
- Initial release with environmental and production data simulation
- Real-time dashboard with WebSocket updates
- 8 Italian regions, 9 crop types
- Realistic seasonal and regional climate modeling
- Interactive time range filtering
- Production analytics with pie chart visualization
