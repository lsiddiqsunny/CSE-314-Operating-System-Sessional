n = int(input())
p = [[] for i in range(n + 1)]
for i in range(n - 1):
    a, b = map(int, input().split())
    p[a].append(b)
    p[b].append(a)
u, v = ' ' + input()[:: 2], ' ' + input()[:: 2]
s, q = [(1, 0, 0, 0)], []
while s:
    a, k, i, j = s.pop()
    if k:
        if i != (u[a] != v[a]):
            q.append(a)
            i = 1 - i
    else:
        if j != (u[a] != v[a]):
            q.append(a)
            j = 1 - j
    k = 1 - k
    for b in p[a]:
        p[b].remove(a)
        s.append((b, k, i, j))
print(len(q))
print('\n'.join(map(str, q)))
