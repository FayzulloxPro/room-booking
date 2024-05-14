const answerEls = document.querySelectorAll('.answer')
const userId = document.getElementById('user_id').value
const submitBtn = document.getElementById('submit')
const questionId = document.getElementById('questionId').value
const contentId = document.getElementById('grammar_id').value
function getSelected() {
    let answer = null
    answerEls.forEach(answerEl => {
        if (answerEl.checked) {
            answer = answerEl.id
            console.log(answer)
        }
    })
    return answer
}
submitBtn.addEventListener('click', () => {
    let optionId = getSelected()
    fetch('http://localhost:8080/practise/grammar/test', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },

        body: JSON.stringify({questionId , optionId, contentId , userId})
    }).then(response => console.log('Success:'))
})
