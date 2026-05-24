export function contentPreview(content = '', maxLength = 96) {
  const normalized = String(content ?? '').replace(/\s+/g, ' ').trim()

  return normalized.length > maxLength ? `${normalized.slice(0, maxLength)}...` : normalized
}

export function relativeTime(dateTime) {
  if (!dateTime) {
    return ''
  }

  const time = new Date(dateTime.replace(' ', 'T')).getTime()
  if (Number.isNaN(time)) {
    return dateTime
  }

  const diffHours = Math.max(1, Math.round((Date.now() - time) / 1000 / 60 / 60))

  if (diffHours < 24) {
    return `${diffHours}h`
  }

  return `${Math.round(diffHours / 24)}d`
}

export function withPostDisplayFields(post) {
  if (!post) {
    return post
  }

  return {
    ...post,
    contentPreview: post.contentPreview || contentPreview(post.content),
    latestReplyTime: post.latestReplyTime || post.updatedAt,
    relativeTime: post.relativeTime || relativeTime(post.updatedAt),
  }
}

export function withReplyDisplayFields(reply) {
  if (!reply) {
    return reply
  }

  return {
    ...reply,
    contentPreview: reply.contentPreview || contentPreview(reply.content),
    href: reply.href || `/posts/${reply.postId}#reply-${reply.id}`,
  }
}
