$(document).ready(function () {

    //League ID 매핑
    var leagueIdMap = {
        "Premier League": "2021",
        "Bundesliga": "2002",
        "Primera Division": "2014",
        "Serie A": "2019",
        "Ligue 1": "2015",
        "UEFA Champions League" : "2001",
        "UEFA Europa League" : "2146",
        "UEFA Conference League" : "2154"
    };

    //Match Schedule - Paging 변수
    let currentLeagueId = null; // 현재 League ID 추적
    let currentPage = 0; // 현재 페이지 번호
    const pageSize = 20; // 페이지 당 표시할 매치 수
    var currentTotalPages = 0; //리그 매치 일정의 전체 페이지 수

    // 로딩시 기본 표시될 콘텐츠
    loadLeagueData("Premier League");


    // 리그 선택에 해당하는 버튼
    $("button.league-button").click(function () {
        var leagueName = $(this).text().trim();
        currentLeagueId = leagueIdMap[leagueName];
        currentPage = 0; // 새 리그 버튼 클릭시 페이지 번호 초기화
        loadLeagueData(leagueName);
    });

    //View - (Standings, Scorers, Fixtures)
    function loadLeagueData(leagueName) {
        var leagueId = leagueIdMap[leagueName];
        if (leagueId) {
            currentLeagueId = leagueId;

            //득점자 순위표 데이터 로드 및 업데이트
            loadLeagueScorers(leagueId);
            //리그 경기 일정 데이터 로드 및 업데이트
            loadLeagueSchedule(leagueId, currentPage);

            //유럽대항전 대회가 선택 되었을 경우
            if(leagueId === "2001") {
                // UEFA_league.html 내용으로 standings 업데이트
                fetch('/uefaLeague/groupStage')
                    .then(response => response.text())
                    .then(html => {
                        document.getElementById('standings').innerHTML = html;
                    })
                    .catch(error => {
                        console.error('Error loading the standings:', error);
                    });
            } else {
                // 다른 리그가 선택된 경우 기존 로직 수행
                //순위표 데이터 로드 및 업데이트
                loadLeagueStandings(leagueId);
            }
        }
    }


    // 순위표 데이터를 로드하고 페이지에 표시하는 함수
    function loadLeagueStandings(leagueId) {
        var requestStandingsUrl = "/standings/" + leagueId;
        $.ajax({
            url: requestStandingsUrl,
            type: "GET",
            success: function(data) {
                // 데이터 처리 및 화면에 표시하는 로직 구현
                console.log(data); // 개발 중 콘솔에 데이터를 출력하여 확인
                updateStandings(data);
            },
            error: function(xhr, status, error) {
                console.error("Error fetching data: ", error);
            }
        });
    }

    // 득점자 순위표 데이터를 로드하고 페이지에 표시하는 함수
    function loadLeagueScorers(leagueId) {
        var requestScorersUrl = "/scorers/" + leagueId;
        $.ajax({
            url: requestScorersUrl,
            type: "GET",
            success: function(data) {
                console.log(data); // 개발 중 콘솔에 데이터를 출력하여 확인
                updateScorers(data);
            },
            error: function(xhr, status, error) {
                console.error("Error fetching data: ", error);
            }
        });
    }

    //리그 별 매치 일정을 로드하는 함수
    function loadLeagueSchedule(leagueId, page) {
        var requestFixturesUrl = "/matches/league/" + leagueId; //리그 경기 일정 URL
        $.ajax({
            url: requestFixturesUrl + `?page=${page}&size=${pageSize}`,
            type: "GET",
            success: function(data) {
                //totalPages 추출
                currentTotalPages = data.totalPages;

                //schedule 업데이트
                updateSchedule(data);

                // 페이지 네비게이션 업데이트 호출 추가
                updatePagination(currentPage, currentTotalPages);

            },
            error: function(xhr, status, error) {
                if(xhr.status === 429){ // Too Many Requests
                    // 서버로부터 받은 응답 메시지를 사용자에게 알림으로 보여줌
                    var responseMessage = xhr.responseText;
                    alert("일시적으로 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요. " + responseMessage);
                } else {
                    // 다른 종류의 에러 처리
                    console.error("Error fetching data: ", error);
                    alert("데이터를 가져오는 중 오류가 발생했습니다.");
                }
            }
        });
    }

    function updatePagination(page, totalPages) {
        let paginationHTML = '';
        for (let i = 0; i < totalPages; i++) {
            // 현재 페이지인 경우 'active' 클래스 추가
            const activeClass = i === page ? 'active' : '';
            paginationHTML += `<a href="#" class="page-num ${activeClass}" data-page="${i}">${i + 1}</a> `;
        }
        document.getElementById('pagination').innerHTML = paginationHTML;

        // 페이지 번호 클릭 이벤트 설정
        document.querySelectorAll('.page-num').forEach(item => {
            item.addEventListener('click', function(e) {
                e.preventDefault();
                const selectedPage = parseInt(this.getAttribute('data-page'));
                currentPage = selectedPage;
                loadLeagueSchedule(currentLeagueId, currentPage);
            });
        });
    }


    //GroupStage -> tournament
    function loadTournamentData() {
        fetch('/uefaLeague/tournament')
            .then(response => response.text())
            .then(html => {
                document.getElementById('standings').innerHTML = html;
            })
            .catch(error => {
                console.error('Error loading the tournament data:', error);
            });
    }

    document.addEventListener('click', function(e) {
        // e.target은 클릭된 요소를 가리킵니다.
        if (e.target.matches('.button') && e.target.getAttribute('href') === '/uefaLeague/tournament') {
            e.preventDefault(); // 기본 이벤트를 방지합니다. (페이지 이동 방지)
            loadTournamentData(); // Tournament 데이터 로드 함수를 호출합니다.
        }
    });

    //Tournament -> GroupStage
    function loadGroupStageData() {
        fetch('/uefaLeague/groupStage')
            .then(response => response.text())
            .then(html => {
                document.getElementById('standings').innerHTML = html;
            })
            .catch(error => {
                console.error('Error loading the tournament data:', error);
            });
    }

    document.addEventListener('click', function(e) {
        // e.target은 클릭된 요소를 가리킵니다.
        if (e.target.matches('.button') && e.target.getAttribute('href') === '/uefaLeague/groupStage') {
            e.preventDefault(); // 기본 이벤트를 방지합니다. (페이지 이동 방지)
            loadGroupStageData(); // Tournament 데이터 로드 함수를 호출합니다.
        }
    });
});


function updateStandings(data) {
    let standingsTable = `
        <table>
            <thead>
                <tr>
                    <th>순위</th>
                    <th>팀</th>
                    <th>경기수</th>
                    <th>승점</th>
                    <th>승</th>
                    <th>무</th>
                    <th>패</th>
                    <th>득점</th>
                    <th>실점</th>
                    <th>득실차</th>
                </tr>
            </thead>
            <tbody>
                ${data.map(team => `
                    <tr>
                        <td>${team.position}</td>
                        <td>
                            <img src="${team.crest}" alt="Team Logo" style="width:20px; height:20px;" />
                            <a href="teamInfo?teamId=${team.teamId}" class="team-link">${team.teamName}</a>
                        </td>
                        <td>${team.playedGames}</td>
                        <td>${team.points}</td>
                        <td>${team.won}</td>
                        <td>${team.draw}</td>
                        <td>${team.lost}</td>
                        <td>${team.goalsFor}</td>
                        <td>${team.goalsAgainst}</td>
                        <td>${team.goalDifference}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    document.getElementById("standings").innerHTML = standingsTable;
}


function updateScorers(data) {
    const scorersTable = `
        <table>
            <thead>
                <tr>
                    <th>순위</th>
                    <th>이름</th>
                    <th>소속팀</th>
                    <th>득점</th>
                    <th>도움</th>
                    <th>패널트킥</th>
                </tr>
            </thead>
            <tbody>
                ${data.map(player => `
                    <tr>
                        <td>${player.position}</td>
                        <td>${player.name}</td>
                        <td>
                            <img src="${player.crest}" alt="Team Logo" style="width:20px; height:20px;" />
                            ${player.teamName}
                        </td>
                        <td>${player.goals}</td>
                        <td>${player.assist}</td>
                        <td>${player.penalties}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    document.getElementById("scorers").innerHTML = scorersTable;
}


function updateSchedule(contents) {
    let matches = contents.content;
    let scheduleTable = `
        <table>
            <thead>
                <tr>
                    <th>날짜 (UTC+9)</th>
                    <th>매치</th>
                    <th>홈 팀</th>
                    <th></th>
                    <th>원정 팀</th>
                    <th>스코어</th>
                </tr>
            </thead>
            <tbody>
                ${matches.map(match => `
                    <tr>
                        <td>${convertToKST(match.matchDate)}</td>
                        <td><img src="${match.matchEmblem}" alt="Match Type" style="width:30px; height:30px;"></td>
                        <td><img src="${match.homeTeamCrest}" alt="Home Team" style="width:20px; height:20px;"> ${match.homeTeamName}</td>
                        <td>vs</td>
                        <td><img src="${match.awayTeamCrest}" alt="Away Team" style="width:20px; height:20px;"> ${match.awayTeamName}</td>
                        <td>${match.homeTeamScore !== null ? match.homeTeamScore : ""} - ${match.awayTeamScore !== null ? match.awayTeamScore : ""}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    document.getElementById("schedule").innerHTML = scheduleTable;
}


// 경기시간을 UTC 기준에서 UTC+9 (한국시간) 으로 변환하는 함수
function convertToKST(dataString) {
    //UTC 시간을 Date 객체로 파싱
    const utcDate = new Date(dataString);

    //UTC -> UTC+9로 변환
    const kstOptions ={
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit',
        hour12: false,
        timeZone: 'Asia/Seoul' //한국 시간대 설정
    }

    return utcDate.toLocaleString('ko-KR', kstOptions).replace(/\.\d+/, '').replace(/-/g, '.').replace(/(\d{4})\.(\d{2})\.(\d{2})/, '$1-$2-$3');
}
