function updateTeamInfo(data) {
    // 팀 엠블럼과 팀명 추출
    var crestUrl = data.crest;
    var teamName = data.teamName;

    $('#teamCrest').attr('src', crestUrl); // 엠블럼을 img 태그의 src 속성으로 설정
    $('#teamName').text(teamName);
}

function updateTeamSquad(squadData) {
    let coachTable = `
        <table>
            <thead>
                <tr>
                    <th>이름</th>
                    <th>출생</th>
                    <th>국적</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>${squadData.coachName}</td>
                    <td>${squadData.coachDateOfBirth}</td>
                    <td>${squadData.coachNationality}</td>
                </tr>
            </tbody>
        </table>
    `;
    document.getElementById("coach-data").innerHTML = coachTable;

    let squadTable = `
        <table>
            <thead>
                <tr>
                    <th>포지션</th>
                    <th>이름</th>
                    <th>출생</th>
                    <th>국적</th>
                </tr>
            </thead>
            <tbody>
                ${squadData.playerDtos.map(player => `
                    <tr>
                        <td>${player.position}</td>
                        <td>${player.name}</td>
                        <td>${player.dateOfBirth}</td>
                        <td>${player.nationality}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    document.getElementById("squad-list").innerHTML = squadTable;

}

function updateTeamMatch(matches) {
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
    document.getElementById("match-list").innerHTML = scheduleTable;
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

$(document).ready(function() {
    // URL에서 teamId 추출
    const queryParams = new URLSearchParams(window.location.search);
    const teamId = queryParams.get('teamId');

    if (teamId) {

        $("#squad-list").html(`teamId : ${teamId}`);
        $("#match-list").html(`teamId : ${teamId}`);

        // 팀 스쿼드 정보 불러오기
        $.ajax({
            url: "/squad/" + teamId,
            type: "GET",
            success: function(data) {
                console.log(data);
                // 엠블럼, 팀명
                updateTeamInfo(data)
                // 코치, 팀 스쿼드
                updateTeamSquad(data)
            },
            error: function(xhr, status, error) {
                console.error("Error fetching data: ", error);
            }
        });

        // 팀 매치 일정 정보 불러오기
        $.ajax({
            url: "/matches/team/" + teamId,
            type: "GET",
            success: function(data) {
                console.log(data);
                // 팀 경기 일정
                updateTeamMatch(data)
            },
            error: function(xhr, status, error) {
                console.error("Error fetching data: ", error);
            }
        });

    } else {
        $("#squad-list").html("팀 ID가 제공되지 않았습니다.");
        $("#match-list").html("팀 ID가 제공되지 않았습니다.");
    }
});
