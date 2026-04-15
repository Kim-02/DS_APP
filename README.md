app/src/main/java/com/example/ds_safer/
├── data/                 # 데이터 소스 및 구현체 (데이터를 가져오는 곳)
│   ├── api/              # Retrofit 인터페이스 (Jetson REST API)
│   ├── mqtt/             # MQTT 클라이언트 및 통신 로직
│   ├── repository/       # Repository 구현체 (데이터 가공 및 전달)
│   ├── model/            # DTO (Data Transfer Object) - JSON 응답용
│   └── local/            # Room DB 또는 DataStore (IP 정보 등 저장)
│
├── domain/               # 비즈니스 로직 (순수 코틀린 코드)
│   ├── model/            # UI에서 사용할 순수 데이터 모델 (Entity)
│   ├── repository/       # Repository 인터페이스 정의
│   └── usecase/          # 센서 등록하기, 카메라 추가하기 등 기능 단위 로직
│
├── ui/                   # Jetpack Compose UI 관련 (화면)
│   ├── components/       # 공통 UI 컴포넌트 (버튼, 카드 등)
│   ├── navigation/       # 화면 이동(NavHost) 설정
│   ├── theme/            # 색상, 폰트, 테마 (프로젝트 생성 시 자동 생성됨)
│   └── screens/          # 실제 화면 단위
│       ├── main/         # 메인 대시보드 화면
│       ├── discovery/    # Jetson 탐색 및 등록 화면
│       └── monitor/      # CCTV 및 센서 실시간 모니터링 화면
│
├── util/                 # 공통 유틸리티 (NSD 탐색기, 확장 함수 등)
│   └── nsd/              # Network Service Discovery (mDNS) 관련 코드
│
└── di/                   # Dependency Injection (Hilt/Koin 설정)